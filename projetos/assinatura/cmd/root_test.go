package cmd

import (
	"bytes"
	"errors"
	"strings"
	"testing"
)

type mockInvoker struct {
	jar    string
	args   []string
	output string
	err    error
}

func (m *mockInvoker) Run(jarPath string, args []string) (string, error) {
	m.jar = jarPath
	m.args = args
	return m.output, m.err
}

func TestVersionCommand(t *testing.T) {
	out := &bytes.Buffer{}
	errOut := &bytes.Buffer{}

	err := ExecuteWithArgs([]string{"version"}, out, errOut, &mockInvoker{})
	if err != nil {
		t.Fatalf("expected nil error, got %v", err)
	}

	if !strings.Contains(out.String(), "Assinatura CLI v") {
		t.Fatalf("expected version output, got %q", out.String())
	}
}

func TestSignMissingRequiredFlags(t *testing.T) {
	out := &bytes.Buffer{}
	errOut := &bytes.Buffer{}

	err := ExecuteWithArgs([]string{"sign"}, out, errOut, &mockInvoker{})
	if err == nil {
		t.Fatalf("expected error for missing required flags")
	}

	if !strings.Contains(err.Error(), "required flag") {
		t.Fatalf("expected required flag error, got %q", err.Error())
	}
}

func TestValidateMissingRequiredFlags(t *testing.T) {
	out := &bytes.Buffer{}
	errOut := &bytes.Buffer{}

	err := ExecuteWithArgs([]string{"validate"}, out, errOut, &mockInvoker{})
	if err == nil {
		t.Fatalf("expected error for missing required flags")
	}

	if !strings.Contains(err.Error(), "required flag") {
		t.Fatalf("expected required flag error, got %q", err.Error())
	}
}

func TestSignCommandMapsParameters(t *testing.T) {
	m := &mockInvoker{output: "assinatura gerada"}
	out := &bytes.Buffer{}
	errOut := &bytes.Buffer{}

	err := ExecuteWithArgs([]string{
		"sign",
		"--jar", "tools/assinador.jar",
		"--input", "entrada.pdf",
		"--output", "entrada.sig",
	}, out, errOut, m)
	if err != nil {
		t.Fatalf("expected nil error, got %v", err)
	}

	if m.jar != "tools/assinador.jar" {
		t.Fatalf("unexpected jar path: %s", m.jar)
	}

	expectedArgs := []string{"sign", "--input", "entrada.pdf", "--output", "entrada.sig"}
	if strings.Join(m.args, " ") != strings.Join(expectedArgs, " ") {
		t.Fatalf("unexpected mapped args: %v", m.args)
	}

	if !strings.Contains(out.String(), "assinatura criada com sucesso") {
		t.Fatalf("expected success message, got %q", out.String())
	}
}

func TestValidateInvalidSignatureReadableOutput(t *testing.T) {
	m := &mockInvoker{output: "assinatura invalida", err: errors.New("exit status 1")}
	out := &bytes.Buffer{}
	errOut := &bytes.Buffer{}

	err := ExecuteWithArgs([]string{
		"validate",
		"--input", "entrada.pdf",
		"--signature", "entrada.sig",
	}, out, errOut, m)
	if err != nil {
		t.Fatalf("expected nil error for invalid signature output, got %v", err)
	}

	if !strings.Contains(out.String(), "INVALIDA") {
		t.Fatalf("expected invalid status output, got %q", out.String())
	}
}

func TestValidateCommandMapsParameters(t *testing.T) {
	m := &mockInvoker{output: "assinatura valida"}
	out := &bytes.Buffer{}
	errOut := &bytes.Buffer{}

	err := ExecuteWithArgs([]string{
		"validate",
		"--jar", "tools/assinador.jar",
		"--input", "entrada.pdf",
		"--signature", "entrada.sig",
	}, out, errOut, m)
	if err != nil {
		t.Fatalf("expected nil error, got %v", err)
	}

	if m.jar != "tools/assinador.jar" {
		t.Fatalf("unexpected jar path: %s", m.jar)
	}

	expectedArgs := []string{"validate", "--input", "entrada.pdf", "--signature", "entrada.sig"}
	if strings.Join(m.args, " ") != strings.Join(expectedArgs, " ") {
		t.Fatalf("unexpected mapped args: %v", m.args)
	}

	if !strings.Contains(out.String(), "VALIDA") {
		t.Fatalf("expected valid output, got %q", out.String())
	}
}
