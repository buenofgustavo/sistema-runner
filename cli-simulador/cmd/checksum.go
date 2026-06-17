package cmd

import (
	"crypto/sha256"
	"fmt"
	"io"
	"log/slog"
	"net/http"
	"os"
	"path/filepath"
	"strings"
	"time"
)

// downloadFileWithSHA256 downloads the file and validates its integrity.
func downloadFileWithSHA256(url, checksumURL, targetPath string) error {
	tempFile, err := os.CreateTemp(filepath.Dir(targetPath), "simulador-download-*")
	if err != nil {
		return fmt.Errorf("erro ao criar arquivo temporário: %w", err)
	}
	tempPath := tempFile.Name()
	defer func() {
		tempFile.Close()
		_ = os.Remove(tempPath)
	}()

	client := &http.Client{Timeout: 15 * time.Minute}
	resp, err := client.Get(url)
	if err != nil {
		return fmt.Errorf("falha ao iniciar o download: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("servidor respondeu com HTTP %d ao tentar baixar o JAR", resp.StatusCode)
	}

	_, err = io.Copy(tempFile, resp.Body)
	if err != nil {
		return fmt.Errorf("falha ao gravar dados do download: %w", err)
	}
	_ = tempFile.Close()

	// 1. Fetch remote checksum
	remoteHash, err := fetchRemoteSHA256(checksumURL)
	if err != nil {
		// Log warning according to specification
		slog.Warn("arquivo de checksum não disponível, continuando sem verificação", "error", err)
		// Proceed without validation
	} else {
		// Calculate SHA-256 of downloaded file
		localHash, err := calculateFileSHA256(tempPath)
		if err != nil {
			return fmt.Errorf("erro ao calcular SHA-256 do arquivo baixado: %w", err)
		}

		if localHash != remoteHash {
			return fmt.Errorf("verificação de integridade do JAR falhou: hash esperado %s, obtido %s", remoteHash, localHash)
		}
		
		slog.Debug("SHA-256 verificado", "hash", localHash)
	}

	// Rename temp file to target path
	if err := os.Rename(tempPath, targetPath); err != nil {
		return fmt.Errorf("falha ao mover arquivo temporário para o destino final: %w", err)
	}

	return nil
}

func fetchRemoteSHA256(url string) (string, error) {
	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Get(url)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("servidor retornou HTTP %d", resp.StatusCode)
	}

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return "", err
	}

	return parseSHA256(string(body))
}

func parseSHA256(content string) (string, error) {
	content = strings.TrimSpace(content)
	// Find the first 64-character hex word
	for _, word := range strings.Fields(content) {
		if len(word) == 64 {
			isHex := true
			for _, r := range word {
				if !((r >= '0' && r <= '9') || (r >= 'a' && r <= 'f') || (r >= 'A' && r <= 'F')) {
					isHex = false
					break
				}
			}
			if isHex {
				return strings.ToLower(word), nil
			}
		}
	}
	return "", fmt.Errorf("formato de checksum sha256 inválido na resposta remota")
}

func calculateFileSHA256(filePath string) (string, error) {
	f, err := os.Open(filePath)
	if err != nil {
		return "", err
	}
	defer f.Close()

	h := sha256.New()
	if _, err := io.Copy(h, f); err != nil {
		return "", err
	}

	return fmt.Sprintf("%x", h.Sum(nil)), nil
}
