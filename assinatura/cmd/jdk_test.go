package cmd

import (
	"runtime"
	"testing"
)

func TestGetAdoptiumURL(t *testing.T) {
	url, format, err := getAdoptiumURL()
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}

	expectedFormat := ".tar.gz"
	if runtime.GOOS == "windows" {
		expectedFormat = ".zip"
	}

	if format != expectedFormat {
		t.Fatalf("expected format %s, got %s", expectedFormat, format)
	}

	if url == "" {
		t.Fatal("expected non-empty URL")
	}
}

func TestStripFirstDir(t *testing.T) {
	tests := []struct {
		input    string
		expected string
	}{
		{"jdk-21/bin/java", "bin/java"},
		{"jdk-21/lib/modules", "lib/modules"},
		{"singlefile.txt", ""},
		{"", ""},
	}

	for _, tc := range tests {
		actual := stripFirstDir(tc.input)
		if actual != tc.expected {
			t.Errorf("stripFirstDir(%q) = %q; expected %q", tc.input, actual, tc.expected)
		}
	}
}
