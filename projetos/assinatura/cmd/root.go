package cmd

import (
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

func NewRootCmd(invoker Invoker) *cobra.Command {
	rootCmd := &cobra.Command{
		Use:           "assinatura",
		Short:         "CLI para assinatura e validacao de artefatos",
		Long:          `Ferramenta de linha de comando para assinar e validar artefatos usando assinador.jar.`,
		SilenceUsage:  true,
		SilenceErrors: true,
	}

	rootCmd.AddCommand(newVersionCmd())
	rootCmd.AddCommand(newSignCmd(invoker))
	rootCmd.AddCommand(newValidateCmd(invoker))

	return rootCmd
}
