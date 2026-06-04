package cmd

import (
	"bytes"
	"errors"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
)

type JavaInvoker struct {
	executor Executor
}

type Executor interface {
	RunCommand(name string, args ...string) (stdout []byte, stderr []byte, exitCode int, err error)
}

type OSExec struct{}

func (e OSExec) RunCommand(name string, args ...string) (stdout []byte, stderr []byte, exitCode int, err error) {
	cmd := exec.Command(name, args...)
	var stdoutBuf, stderrBuf bytes.Buffer
	cmd.Stdout = &stdoutBuf
	cmd.Stderr = &stderrBuf
	err = cmd.Run()
	stdout = stdoutBuf.Bytes()
	stderr = stderrBuf.Bytes()
	if err != nil {
		var exitErr *exec.ExitError
		if errors.As(err, &exitErr) {
			exitCode = exitErr.ExitCode()
		} else {
			exitCode = -1
		}
	} else {
		exitCode = 0
	}
	return
}

type ExitCodeError struct {
	ExitCode int
	Err      error
}

func (e *ExitCodeError) Error() string {
	return e.Err.Error()
}

func (e *ExitCodeError) Unwrap() error {
	return e.Err
}

func NewJavaInvoker() *JavaInvoker {
	return &JavaInvoker{executor: OSExec{}}
}

func (j *JavaInvoker) Run(jarPath string, args []string) (string, error) {
	javaPath, err := findJava()
	if err != nil {
		// Java 21+ não encontrado, tentar provisionar automaticamente
		provisionErr := EnsureJDK()
		if provisionErr != nil {
			return "", fmt.Errorf("java nao encontrado e falha ao provisionar: %w (erro original: %v)", provisionErr, err)
		}
		javaPath, err = findJava()
		if err != nil {
			return "", fmt.Errorf("java nao encontrado apos o provisionamento: %w", err)
		}
	}

	if _, err := os.Stat(jarPath); err != nil {
		if errors.Is(err, os.ErrNotExist) {
			return "", fmt.Errorf("arquivo jar nao encontrado: %s", jarPath)
		}
		return "", fmt.Errorf("nao foi possivel acessar o jar: %w", err)
	}

	fullArgs := append([]string{"-jar", jarPath}, args...)
	stdout, stderr, exitCode, err := j.executor.RunCommand(javaPath, fullArgs...)
	
	// Propaga o stderr do processo Java diretamente para o stderr do processo Go
	if len(stderr) > 0 {
		_, _ = os.Stderr.Write(stderr)
	}

	if err != nil {
		return string(stdout), &ExitCodeError{
			ExitCode: exitCode,
			Err:      fmt.Errorf("falha ao executar java -jar: %w", err),
		}
	}

	return string(stdout), nil
}

func checkJavaVersion(javaPath string) error {
	cmd := exec.Command(javaPath, "-version")
	var stderrBuf bytes.Buffer
	cmd.Stderr = &stderrBuf
	err := cmd.Run()
	if err != nil {
		return fmt.Errorf("falha ao executar %s -version: %w", javaPath, err)
	}

	versionStr := stderrBuf.String()
	// Procurar por `version "X.Y.Z"`
	idx := strings.Index(versionStr, "version \"")
	if idx == -1 {
		return fmt.Errorf("nao foi possivel identificar a versao do Java na saida: %s", versionStr)
	}

	start := idx + len("version \"")
	end := strings.Index(versionStr[start:], "\"")
	if end == -1 {
		return fmt.Errorf("nao foi possivel identificar a versao do Java na saida: %s", versionStr)
	}

	fullVersion := versionStr[start : start+end]
	parts := strings.Split(fullVersion, ".")
	if len(parts) == 0 {
		return fmt.Errorf("versao invalida do Java: %s", fullVersion)
	}

	majorStr := parts[0]
	if majorStr == "1" && len(parts) > 1 {
		majorStr = parts[1]
	}

	var major int
	_, err = fmt.Sscanf(majorStr, "%d", &major)
	if err != nil {
		return fmt.Errorf("falha ao analisar versao do Java '%s': %w", fullVersion, err)
	}

	if major < 21 {
		return fmt.Errorf("versao do Java %d e inferior a 21. Requer versao >= 21", major)
	}

	return nil
}

func findJava() (string, error) {
	// 1. Buscar em ~/.assinatura/jre/ primeiro
	if homeDir, err := os.UserHomeDir(); err == nil {
		localJreDir := filepath.Join(homeDir, ".assinatura", "jre")
		
		// Verificacao direta padrao
		directPath := filepath.Join(localJreDir, "bin", "java")
		if _, err := os.Stat(directPath); err == nil {
			if errVer := checkJavaVersion(directPath); errVer == nil {
				return directPath, nil
			}
		}
		directPathExe := directPath + ".exe"
		if _, err := os.Stat(directPathExe); err == nil {
			if errVer := checkJavaVersion(directPathExe); errVer == nil {
				return directPathExe, nil
			}
		}
		
		// Verificacao em subpastas caso esteja aninhado
		var foundPath string
		_ = filepath.Walk(localJreDir, func(path string, info os.FileInfo, err error) error {
			if err != nil {
				return nil
			}
			base := filepath.Base(path)
			if !info.IsDir() && (base == "java" || base == "java.exe") {
				parent := filepath.Base(filepath.Dir(path))
				if parent == "bin" {
					if errVer := checkJavaVersion(path); errVer == nil {
						foundPath = path
						return filepath.SkipAll
					}
				}
			}
			return nil
		})
		if foundPath != "" {
			return foundPath, nil
		}
	}

	// 2. Buscar no PATH padrao
	if path, err := exec.LookPath("java"); err == nil {
		if errVer := checkJavaVersion(path); errVer == nil {
			return path, nil
		}
	}

	// 3. Buscar via JAVA_HOME
	javaHome := os.Getenv("JAVA_HOME")
	if javaHome != "" {
		candidate := filepath.Join(javaHome, "bin", "java")
		if _, err := os.Stat(candidate); err == nil {
			if errVer := checkJavaVersion(candidate); errVer == nil {
				return candidate, nil
			}
		}

		candidateExe := candidate + ".exe"
		if _, err := os.Stat(candidateExe); err == nil {
			if errVer := checkJavaVersion(candidateExe); errVer == nil {
				return candidateExe, nil
			}
		}
	}

	return "", errors.New("java 21+ nao encontrado. Instale o Java 21 ou superior")
}
