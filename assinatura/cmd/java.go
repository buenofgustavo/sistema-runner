package cmd

import (
	"errors"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
)

type JavaInvoker struct {
	executor Executor
}

type Executor interface {
	CombinedOutput(name string, args ...string) ([]byte, error)
}

type OSExec struct{}

func (e OSExec) CombinedOutput(name string, args ...string) ([]byte, error) {
	return exec.Command(name, args...).CombinedOutput()
}

func NewJavaInvoker() *JavaInvoker {
	return &JavaInvoker{executor: OSExec{}}
}

func (j *JavaInvoker) Run(jarPath string, args []string) (string, error) {
	javaPath, err := findJava()
	if err != nil {
		// Java não encontrado, tentar provisionar automaticamente
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
	output, err := j.executor.CombinedOutput(javaPath, fullArgs...)
	if err != nil {
		return string(output), fmt.Errorf("falha ao executar java -jar: %w", err)
	}

	return string(output), nil
}

func findJava() (string, error) {
	// 1. Buscar em ~/.assinatura/jre/ primeiro
	if homeDir, err := os.UserHomeDir(); err == nil {
		localJreDir := filepath.Join(homeDir, ".assinatura", "jre")
		
		// Verificacao direta padrao
		directPath := filepath.Join(localJreDir, "bin", "java")
		if _, err := os.Stat(directPath); err == nil {
			return directPath, nil
		}
		directPathExe := directPath + ".exe"
		if _, err := os.Stat(directPathExe); err == nil {
			return directPathExe, nil
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
					foundPath = path
					return filepath.SkipAll
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
		return path, nil
	}

	// 3. Buscar via JAVA_HOME
	javaHome := os.Getenv("JAVA_HOME")
	if javaHome != "" {
		candidate := filepath.Join(javaHome, "bin", "java")
		if _, err := os.Stat(candidate); err == nil {
			return candidate, nil
		}

		candidateExe := candidate + ".exe"
		if _, err := os.Stat(candidateExe); err == nil {
			return candidateExe, nil
		}
	}

	return "", errors.New("java nao encontrado. Instale um JDK/JRE ou configure JAVA_HOME")
}
