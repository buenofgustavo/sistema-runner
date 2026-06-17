//go:build !windows

package cmd

import (
	"os"
	"os/exec"
	"syscall"
)

func isProcessRunning(pid int) bool {
	if pid <= 0 {
		return false
	}
	process, err := os.FindProcess(pid)
	if err != nil {
		return false
	}
	err = process.Signal(syscall.Signal(0))
	return err == nil
}

func detachProcess(cmd *exec.Cmd) {
	// No special background detaching required for UNIX platforms
}

