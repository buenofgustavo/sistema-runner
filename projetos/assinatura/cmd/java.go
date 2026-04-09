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
		return "", err
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
	if path, err := exec.LookPath("java"); err == nil {
		return path, nil
	}

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
