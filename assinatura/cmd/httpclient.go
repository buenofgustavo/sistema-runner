package cmd

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"time"
)

type HttpInvoker struct {
	Port    string
	JarPath string
}

func NewHttpInvoker(port string, jarPath string) *HttpInvoker {
	if port == "" {
		port = "8081"
	}
	if jarPath == "" {
		jarPath = "assinador.jar"
	}
	return &HttpInvoker{Port: port, JarPath: jarPath}
}

func (h *HttpInvoker) Run(unusedJarPath string, args []string) (string, error) {
	// 1. Garantir que o servidor está rodando
	err := h.ensureServerRunning()
	if err != nil {
		return "", fmt.Errorf("falha ao garantir servidor rodando: %w", err)
	}

	if len(args) < 1 {
		return "", fmt.Errorf("argumentos insuficientes")
	}

	operation := args[0]
	client := &http.Client{Timeout: 10 * time.Second}

	switch operation {
	case "sign":
		// Analisar argumentos: sign --input <path> --output <path>
		var inputPath, outputPath string
		for i := 0; i < len(args); i++ {
			if args[i] == "--input" && i+1 < len(args) {
				inputPath = args[i+1]
			} else if args[i] == "--output" && i+1 < len(args) {
				outputPath = args[i+1]
			}
		}

		if inputPath == "" || outputPath == "" {
			return "", fmt.Errorf("parametros --input e --output sao obrigatorios")
		}

		// Ler arquivo de entrada
		contentBytes, err := os.ReadFile(inputPath)
		if err != nil {
			return "", fmt.Errorf("erro ao ler arquivo de entrada: %w", err)
		}

		// Montar requisição JSON
		reqPayload := map[string]string{
			"content": string(contentBytes),
		}
		reqBytes, err := json.Marshal(reqPayload)
		if err != nil {
			return "", err
		}

		url := fmt.Sprintf("http://localhost:%s/api/sign", h.Port)
		resp, err := client.Post(url, "application/json", bytes.NewBuffer(reqBytes))
		if err != nil {
			return "", fmt.Errorf("erro na requisicao HTTP: %w", err)
		}
		defer resp.Body.Close()

		respBytes, err := io.ReadAll(resp.Body)
		if err != nil {
			return "", err
		}

		if resp.StatusCode != http.StatusOK {
			return string(respBytes), fmt.Errorf("servidor retornou erro (HTTP %d)", resp.StatusCode)
		}

		// Parsear resposta
		var signResp struct {
			Signature string `json:"signature"`
			Valid     bool   `json:"valid"`
			Message   string `json:"message"`
		}
		if err := json.Unmarshal(respBytes, &signResp); err != nil {
			return string(respBytes), fmt.Errorf("erro ao ler resposta do servidor: %w", err)
		}

		if !signResp.Valid {
			return signResp.Message, fmt.Errorf("assinatura invalida pelo servidor: %s", signResp.Message)
		}

		// Gravar assinatura no arquivo de saída
		err = os.WriteFile(outputPath, []byte(signResp.Signature), 0644)
		if err != nil {
			return "", fmt.Errorf("erro ao gravar arquivo de assinatura: %w", err)
		}

		return fmt.Sprintf("Status: %s\nAssinatura: %s", signResp.Message, signResp.Signature), nil

	case "validate":
		// Analisar argumentos: validate --input <path> --signature <path>
		var inputPath, signaturePath string
		for i := 0; i < len(args); i++ {
			if args[i] == "--input" && i+1 < len(args) {
				inputPath = args[i+1]
			} else if args[i] == "--signature" && i+1 < len(args) {
				signaturePath = args[i+1]
			}
		}

		if inputPath == "" || signaturePath == "" {
			return "", fmt.Errorf("parametros --input e --signature sao obrigatorios")
		}

		// Ler conteúdo original
		contentBytes, err := os.ReadFile(inputPath)
		if err != nil {
			return "", fmt.Errorf("erro ao ler arquivo original: %w", err)
		}

		// Ler assinatura
		sigBytes, err := os.ReadFile(signaturePath)
		if err != nil {
			return "", fmt.Errorf("erro ao ler arquivo de assinatura: %w", err)
		}

		// Montar requisição JSON
		reqPayload := map[string]string{
			"content":   string(contentBytes),
			"signature": string(sigBytes),
		}
		reqBytes, err := json.Marshal(reqPayload)
		if err != nil {
			return "", err
		}

		url := fmt.Sprintf("http://localhost:%s/api/validate", h.Port)
		resp, err := client.Post(url, "application/json", bytes.NewBuffer(reqBytes))
		if err != nil {
			return "", fmt.Errorf("erro na requisicao HTTP: %w", err)
		}
		defer resp.Body.Close()

		respBytes, err := io.ReadAll(resp.Body)
		if err != nil {
			return "", err
		}

		if resp.StatusCode != http.StatusOK {
			return string(respBytes), fmt.Errorf("servidor retornou erro (HTTP %d)", resp.StatusCode)
		}

		// Parsear resposta
		var valResp struct {
			Valid   bool   `json:"valid"`
			Message string `json:"message"`
		}
		if err := json.Unmarshal(respBytes, &valResp); err != nil {
			return string(respBytes), fmt.Errorf("erro ao ler resposta do servidor: %w", err)
		}

		if !valResp.Valid {
			// Retornamos um erro que contenha exit status ou similar para o validate.go tratar como ASSINATURA INVALIDA
			return valResp.Message, fmt.Errorf("exit status 1: %s", valResp.Message)
		}

		return fmt.Sprintf("Status: %s", valResp.Message), nil

	default:
		return "", fmt.Errorf("operacao nao suportada por HTTP: %s", operation)
	}
}

func (h *HttpInvoker) ensureServerRunning() error {
	healthUrl := fmt.Sprintf("http://localhost:%s/api/health", h.Port)
	if isServerRunning(healthUrl) {
		return nil
	}

	// Tenta obter o executável java
	javaPath, err := findJava()
	if err != nil {
		// Java não encontrado, tentar provisionar automaticamente
		provisionErr := EnsureJDK()
		if provisionErr != nil {
			return fmt.Errorf("java nao encontrado e falha ao provisionar: %w (erro original: %v)", provisionErr, err)
		}
		javaPath, err = findJava()
		if err != nil {
			return fmt.Errorf("java nao encontrado apos o provisionamento: %w", err)
		}
	}

	// Inicia o processo do servidor em background
	process := exec.Command(javaPath, "-jar", h.JarPath, "--server", "--port", h.Port)
	process.Stdout = nil
	process.Stderr = nil

	err = process.Start()
	if err != nil {
		return fmt.Errorf("falha ao iniciar servidor em background: %w", err)
	}

	// Aguarda até 5 segundos para confirmação
	for i := 0; i < 10; i++ {
		time.Sleep(500 * time.Millisecond)
		if isServerRunning(healthUrl) {
			return nil
		}
	}

	return fmt.Errorf("servidor iniciou mas nao respondeu na porta %s", h.Port)
}
