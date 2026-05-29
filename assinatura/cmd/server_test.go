package cmd

import (
	"bytes"
	"strings"
	"testing"
)

func TestServerCommandsLoaded(t *testing.T) {
	root := NewRootCmd(&mockInvoker{})
	
	// Verificar se comando server foi carregado
	serverCmd, _, err := root.Find([]string{"server"})
	if err != nil {
		t.Fatalf("expected server command to be registered: %v", err)
	}
	
	if serverCmd.Name() != "server" {
		t.Fatalf("unexpected command name: %s", serverCmd.Name())
	}

	// Verificar subcomandos
	if serverCmd.HasSubCommands() {
		subCommands := serverCmd.Commands()
		expectedNames := map[string]bool{"start": true, "stop": true, "status": true}
		for _, sub := range subCommands {
			delete(expectedNames, sub.Name())
		}
		if len(expectedNames) > 0 {
			t.Fatalf("missing server subcommands: %v", expectedNames)
		}
	} else {
		t.Fatal("server command should have subcommands")
	}
}

func TestServerStatusOffline(t *testing.T) {
	out := &bytes.Buffer{}
	errOut := &bytes.Buffer{}

	err := ExecuteWithArgs([]string{"server", "status", "--port", "9999"}, out, errOut, &mockInvoker{})
	if err != nil {
		t.Fatalf("expected nil error, got %v", err)
	}

	if !strings.Contains(out.String(), "NÃO está em execução") {
		t.Fatalf("expected offline status, got %q", out.String())
	}
}

func TestServerStopOffline(t *testing.T) {
	out := &bytes.Buffer{}
	errOut := &bytes.Buffer{}

	err := ExecuteWithArgs([]string{"server", "stop", "--port", "9999"}, out, errOut, &mockInvoker{})
	if err != nil {
		t.Fatalf("expected nil error, got %v", err)
	}

	if !strings.Contains(out.String(), "Não foi possível conectar") {
		t.Fatalf("expected connection error or offline status, got %q", out.String())
	}
}
