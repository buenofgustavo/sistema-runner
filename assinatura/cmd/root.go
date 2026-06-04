package cmd

import (
	"errors"
	"fmt"
	"io"
	"os"

	"github.com/spf13/cobra"
)

type Invoker interface {
	Run(jarPath string, args []string) (string, error)
}

func Execute() {
	err := ExecuteWithArgs(os.Args[1:], os.Stdout, os.Stderr, NewJavaInvoker())
	if err != nil {
		_, _ = fmt.Fprintln(os.Stderr, err)
		var exitCodeErr *ExitCodeError
		if errors.As(err, &exitCodeErr) {
			os.Exit(exitCodeErr.ExitCode)
		}
		os.Exit(1)
	}
}

func ExecuteWithArgs(args []string, out io.Writer, errOut io.Writer, invoker Invoker) error {
	rootCmd := NewRootCmd(invoker)
	rootCmd.SetArgs(args)
	rootCmd.SetOut(out)
	rootCmd.SetErr(errOut)
	return rootCmd.Execute()
}

var (
	Verbose bool
	Quiet   bool
)

func NewRootCmd(invoker Invoker) *cobra.Command {
	rootCmd := &cobra.Command{
		Use:           "assinatura",
		Short:         "CLI para assinatura e validacao de artefatos",
		Long:          `Ferramenta de linha de comando para assinar e validar artefatos usando assinador.jar.`,
		Version:       fmt.Sprintf("v%s+commit.%s", version, gitCommit),
		SilenceUsage:  true,
		SilenceErrors: true,
	}

	rootCmd.PersistentFlags().BoolVarP(&Verbose, "verbose", "v", false, "Habilita logs detalhados e saida do processo Java")
	rootCmd.PersistentFlags().BoolVarP(&Quiet, "quiet", "q", false, "Silencia mensagens de status intermediarias")

	rootCmd.AddCommand(newVersionCmd())
	rootCmd.AddCommand(newSignCmd(invoker))
	rootCmd.AddCommand(newValidateCmd(invoker))
	rootCmd.AddCommand(newServerCmd())

	return rootCmd
}
