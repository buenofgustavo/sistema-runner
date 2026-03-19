package runner.assinador;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import runner.assinador.dto.*;
import runner.assinador.service.AssinaturaService;

import java.util.Arrays;

@SpringBootApplication
public class AssinadorApplication implements CommandLineRunner {

	private final AssinaturaService service;

	public AssinadorApplication(AssinaturaService service) {
		this.service = service;
	}

	public static void main(String[] args) {
		SpringApplication.run(AssinadorApplication.class, args);
	}

	@Override
	public void run(String... args) {

		// 👉 Se NÃO tiver argumentos → roda como API normal
		if (args.length == 0) {
			System.out.println("Modo HTTP iniciado (API REST)");
			return;
		}

		try {
			String comando = args[0];

			switch (comando.toLowerCase()) {

				case "criar":
					executarCriar(args);
					break;

				case "validar":
					executarValidar(args);
					break;

				default:
					System.out.println("Comando inválido. Use: criar | validar");
			}

		} catch (Exception e) {
			System.out.println("Erro: " + e.getMessage());
		}

		// 👉 FINALIZA a aplicação após executar CLI
		System.exit(0);
	}

	// ===============================
	// 🔹 CRIAR
	// ===============================
	private void executarCriar(String[] args) {

		CriarAssinaturaRequest req = new CriarAssinaturaRequest();

		// 🔥 parsing simples dos argumentos
		for (int i = 1; i < args.length; i += 2) {
			String chave = args[i];
			String valor = args[i + 1];

			switch (chave) {
				case "--type":
					CodingDTO coding = new CodingDTO();
					coding.setSystem("system");
					coding.setCode(valor);
					req.setType(Arrays.asList(coding));
					break;

				case "--when":
					req.setWhen(valor);
					break;

				case "--who":
					req.setWhoUri(valor);
					break;

				case "--target":
					ReferenceDTO ref = new ReferenceDTO();
					ref.setReference(valor);
					req.setTarget(Arrays.asList(ref));
					break;

				case "--sigFormat":
					req.setSigFormat(valor);
					break;

				case "--data":
					req.setData(valor);
					break;
			}
		}

		AssinaturaResponse response = service.criar(req);

		System.out.println("✔ Sucesso: " + response.isSucesso());
		System.out.println("✔ Assinatura: " + response.getData());
	}

	// ===============================
	// 🔹 VALIDAR
	// ===============================
	private void executarValidar(String[] args) {

		ValidarAssinaturaRequest req = new ValidarAssinaturaRequest();

		for (int i = 1; i < args.length; i += 2) {
			String chave = args[i];
			String valor = args[i + 1];

			switch (chave) {
				case "--signature":
					req.setSignature(valor);
					break;

				case "--type":
					CodingDTO coding = new CodingDTO();
					coding.setSystem("system");
					coding.setCode(valor);
					req.setType(Arrays.asList(coding));
					break;

				case "--when":
					req.setWhen(valor);
					break;

				case "--who":
					req.setWhoUri(valor);
					break;

				case "--target":
					ReferenceDTO ref = new ReferenceDTO();
					ref.setReference(valor);
					req.setTarget(Arrays.asList(ref));
					break;

				case "--sigFormat":
					req.setSigFormat(valor);
					break;

				case "--data":
					req.setData(valor);
					break;
			}
		}

		ValidacaoResponse response = service.validar(req);

		System.out.println("✔ Resultado: " + response.isSucesso());
		System.out.println("✔ Mensagem: " + response.getMensagem());
	}
}