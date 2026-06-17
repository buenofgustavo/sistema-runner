package cmd

import (
	"fmt"
	"io"
	"log/slog"
	"os"

	"github.com/spf13/cobra"
)

var (
	Verbose bool
)

func Execute() {
	if err := ExecuteWithArgs(os.Args[1:], os.Stdout, os.Stderr); err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
}

func ExecuteWithArgs(args []string, out io.Writer, errOut io.Writer) error {
	rootCmd := NewRootCmd()
	rootCmd.SetArgs(args)
	rootCmd.SetOut(out)
	rootCmd.SetErr(errOut)
	return rootCmd.Execute()
}

func NewRootCmd() *cobra.Command {
	rootCmd := &cobra.Command{
		Use:           "simulador",
		Short:         "CLI para gerenciar o simulador do HubSaúde",
		Long:          `Esta ferramenta permite iniciar, parar e verificar o servidor simulador do HubSaúde.`,
		SilenceUsage:  true,
		SilenceErrors: true,
		PersistentPreRun: func(cmd *cobra.Command, args []string) {
			setupLogging()
		},
	}

	rootCmd.PersistentFlags().BoolVar(&Verbose, "verbose", false, "Exibe informações extras de diagnóstico")

	rootCmd.AddCommand(newStartCmd())
	rootCmd.AddCommand(newStatusCmd())
	rootCmd.AddCommand(newStopCmd())
	rootCmd.AddCommand(newVersionCmd())

	return rootCmd
}

func setupLogging() {
	level := slog.LevelInfo
	if Verbose {
		level = slog.LevelDebug
	}

	opts := &slog.HandlerOptions{
		Level: level,
	}
	handler := slog.NewTextHandler(os.Stderr, opts)
	slog.SetDefault(slog.New(handler))
}
