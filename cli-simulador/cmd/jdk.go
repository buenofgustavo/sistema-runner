package cmd

import (
	"archive/tar"
	"archive/zip"
	"compress/gzip"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"runtime"
	"strings"
	"time"
)

// URLs estáveis da API do Adoptium para JRE 21 LTS
const (
	urlWindowsX64 = "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jre/hotspot/normal/eclipse"
	urlLinuxX64   = "https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jre/hotspot/normal/eclipse"
	urlMacX64     = "https://api.adoptium.net/v3/binary/latest/21/ga/mac/x64/jre/hotspot/normal/eclipse"
)

// EnsureJDK verifica se o Java 21 está instalado e, caso contrário, realiza o provisionamento automático.
func EnsureJDK() error {
	_, err := findJava()
	if err == nil {
		// Java já está disponível
		return nil
	}

	// Não encontrado, vamos provisionar na pasta ~/.hubsaude/jdk
	homeDir, err := os.UserHomeDir()
	if err != nil {
		return fmt.Errorf("não foi possível obter diretório do usuário: %w", err)
	}

	targetDir := filepath.Join(homeDir, ".hubsaude", "jdk")
	fmt.Printf("Java não detectado. Iniciando provisionamento automático do JRE 21 em %s...\n", targetDir)

	err = os.MkdirAll(targetDir, 0755)
	if err != nil {
		return fmt.Errorf("falha ao criar pasta de destino: %w", err)
	}

	url, format, err := getAdoptiumURL()
	if err != nil {
		return err
	}

	// Criar arquivo temporário para download
	tempFile, err := os.CreateTemp("", "jdk-download-*"+format)
	if err != nil {
		return fmt.Errorf("erro ao criar arquivo temporário: %w", err)
	}
	defer os.Remove(tempFile.Name())
	defer tempFile.Close()

	fmt.Printf("Baixando JRE de: %s\n", url)
	err = downloadFile(url, tempFile)
	if err != nil {
		return fmt.Errorf("falha ao baixar JRE: %w", err)
	}

	// Voltar ao início do arquivo para leitura
	_, err = tempFile.Seek(0, 0)
	if err != nil {
		return err
	}

	fmt.Println("Extraindo JRE...")
	if format == ".zip" {
		err = extractZip(tempFile, targetDir)
	} else {
		err = extractTarGz(tempFile, targetDir)
	}

	if err != nil {
		return fmt.Errorf("falha ao extrair JRE: %w", err)
	}

	fmt.Println("JRE 21 provisionado com sucesso!")
	return nil
}

func getAdoptiumURL() (string, string, error) {
	osType := runtime.GOOS
	arch := runtime.GOARCH

	// Para esta especificação, suportamos amd64 (x64)
	if arch != "amd64" {
		fmt.Printf("Aviso: arquitetura %s detectada, baixando versão x64/amd64 por padrão\n", arch)
	}

	switch osType {
	case "windows":
		return urlWindowsX64, ".zip", nil
	case "linux":
		return urlLinuxX64, ".tar.gz", nil
	case "darwin":
		return urlMacX64, ".tar.gz", nil
	default:
		return "", "", fmt.Errorf("sistema operacional não suportado para provisionamento automático: %s", osType)
	}
}

func downloadFile(url string, target io.Writer) error {
	client := &http.Client{Timeout: 10 * time.Minute}
	resp, err := client.Get(url)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("servidor respondeu com HTTP %d", resp.StatusCode)
	}

	_, err = io.Copy(target, resp.Body)
	return err
}

func extractZip(file *os.File, targetDir string) error {
	stat, err := file.Stat()
	if err != nil {
		return err
	}

	reader, err := zip.NewReader(file, stat.Size())
	if err != nil {
		return err
	}

	for _, zipFile := range reader.File {
		// Ignorar o primeiro componente do path (ex: jdk-21.0.10+8-jre/)
		strippedPath := stripFirstDir(zipFile.Name)
		if strippedPath == "" {
			continue
		}

		path := filepath.Join(targetDir, strippedPath)

		if zipFile.FileInfo().IsDir() {
			err = os.MkdirAll(path, 0755)
			if err != nil {
				return err
			}
			continue
		}

		err = os.MkdirAll(filepath.Dir(path), 0755)
		if err != nil {
			return err
		}

		outFile, err := os.OpenFile(path, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, zipFile.Mode())
		if err != nil {
			return err
		}

		rc, err := zipFile.Open()
		if err != nil {
			outFile.Close()
			return err
		}

		_, err = io.Copy(outFile, rc)
		rc.Close()
		outFile.Close()
		if err != nil {
			return err
		}
	}
	return nil
}

func extractTarGz(file io.Reader, targetDir string) error {
	gzipReader, err := gzip.NewReader(file)
	if err != nil {
		return err
	}
	defer gzipReader.Close()

	tarReader := tar.NewReader(gzipReader)

	for {
		header, err := tarReader.Next()
		if err == io.EOF {
			break
		}
		if err != nil {
			return err
		}

		strippedPath := stripFirstDir(header.Name)
		if strippedPath == "" {
			continue
		}

		path := filepath.Join(targetDir, strippedPath)

		switch header.Typeflag {
		case tar.TypeDir:
			err = os.MkdirAll(path, 0755)
			if err != nil {
				return err
			}
		case tar.TypeReg:
			err = os.MkdirAll(filepath.Dir(path), 0755)
			if err != nil {
				return err
			}

			outFile, err := os.OpenFile(path, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, header.FileInfo().Mode())
			if err != nil {
				return err
			}

			_, err = io.Copy(outFile, tarReader)
			outFile.Close()
			if err != nil {
				return err
			}
		}
	}
	return nil
}

func stripFirstDir(path string) string {
	parts := strings.Split(filepath.ToSlash(path), "/")
	if len(parts) <= 1 {
		return ""
	}
	return filepath.Join(parts[1:]...)
}
