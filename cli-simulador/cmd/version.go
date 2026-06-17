package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
)

var (
	version   = "2.0.1"
	gitCommit = "abc1234"
)

func newVersionCmd() *cobra.Command {
	return &cobra.Command{
		Use:   "version",
		Short: "Exibe a versão atual do simulador",
		Run: func(cmd *cobra.Command, args []string) {
			if gitCommit == "" {
				fmt.Fprintf(cmd.OutOrStdout(), "simulador v%s\n", version)
			} else {
				fmt.Fprintf(cmd.OutOrStdout(), "simulador v%s (commit %s)\n", version, gitCommit)
			}
		},
	}
}
