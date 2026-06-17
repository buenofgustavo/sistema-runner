package cmd

import (
	"bytes"
	"strings"
	"testing"
)

func TestVersionCommand(t *testing.T) {
	out := &bytes.Buffer{}
	errOut := &bytes.Buffer{}

	err := ExecuteWithArgs([]string{"version"}, out, errOut)
	if err != nil {
		t.Fatalf("expected nil error, got %v", err)
	}

	if !strings.Contains(out.String(), "simulador v") {
		t.Fatalf("expected version output, got %q", out.String())
	}
}

func TestUnknownCommand(t *testing.T) {
	out := &bytes.Buffer{}
	errOut := &bytes.Buffer{}

	err := ExecuteWithArgs([]string{"nonexistent"}, out, errOut)
	if err == nil {
		t.Fatalf("expected error for nonexistent command")
	}
}
