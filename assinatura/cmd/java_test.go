package cmd

import (
	"errors"
	"strings"
	"testing"
)

type fakeExecutor struct {
	name   string
	args   []string
	output []byte
	err    error
}

func (f *fakeExecutor) CombinedOutput(name string, args ...string) ([]byte, error) {
	f.name = name
	f.args = args
	return f.output, f.err
}

func TestJavaInvokerJarNotFound(t *testing.T) {
	invoker := &JavaInvoker{executor: &fakeExecutor{}}
	_, err := invoker.Run("arquivo-inexistente.jar", []string{"sign"})
	if err == nil {
		t.Fatal("expected error for missing jar")
	}
	if !strings.Contains(err.Error(), "jar nao encontrado") {
		t.Fatalf("unexpected error: %v", err)
	}
}

func TestJavaInvokerCommandErrorReturnsToolOutput(t *testing.T) {
	invoker := &JavaInvoker{executor: &fakeExecutor{output: []byte("erro do assinador"), err: errors.New("exit status 1")}}
	_, err := invoker.Run("java_test.go", []string{"validate"})
	if err == nil {
		t.Fatal("expected execution error")
	}
	if !strings.Contains(err.Error(), "falha ao executar java -jar") {
		t.Fatalf("unexpected error: %v", err)
	}
}
