package runner.assinador;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.WebApplicationType;

@SpringBootApplication
public class AssinadorApplication {

	public static void main(String[] args) {

		// Se tiver argumentos → modo CLI
		if (args.length > 0) {
			SpringApplication app = new SpringApplication(AssinadorApplication.class);
			app.setWebApplicationType(WebApplicationType.NONE); // não sobe servidor
			app.run(args);
		} else {
			// Sem argumentos → modo HTTP
			SpringApplication.run(AssinadorApplication.class, args);
		}
	}

	@Bean
	public CommandLineRunner run() {
		return args -> {

			// Se não tiver argumentos, não faz nada (modo HTTP)
			if (args.length == 0) {
				return;
			}

			String comando = args[0];

			if ("criar".equalsIgnoreCase(comando)) {

				// Simulação de criação
				System.out.println("ASSINATURA_FAKE");

			} else if ("validar".equalsIgnoreCase(comando)) {

				if (args.length < 2) {
					System.out.println("Erro: informe a assinatura");
					return;
				}

				String assinatura = args[1];

				boolean valida = "ASSINATURA_FAKE".equals(assinatura);

				System.out.println(
						valida ? "Assinatura válida" : "Assinatura inválida"
				);

			} else {
				System.out.println("Comando inválido");
			}

			// Encerra aplicação (importante pro CLI)
			System.exit(0);
		};
	}
}