package opyta.bdo.bdoBot;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import opyta.bdo.bdoBot.models.Client;
import opyta.bdo.bdoBot.repositories.ClientRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.regex.Pattern;

@SpringBootApplication
@PropertySource("classpath:.env")
public class BdoBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(BdoBotApplication.class, args);
	}

	@Value("${TOKEN}")
	private String token;

	@Value("${URL}")
	private String url;

	@Bean
	public CommandLineRunner initData(ClientRepository clientRepository) {
		return args -> {

			DiscordClient client = DiscordClient.create(token);
			GatewayDiscordClient gateway = client.login().block();

			System.out.println("Ready to read commands");

			gateway.on(MessageCreateEvent.class).subscribe(event -> {
				Message message = event.getMessage();
				Member member = event.getMember().orElse(null);

				if (member != null && "verify".equals(message.getContent())) {
					MessageChannel channel = message.getChannel().block();



					// Obtener el ID del usuario y su nickname
					long userId = member.getId().asLong();
					String nickname = member.getNickname().orElse(null);

					System.out.println(nickname);

					String region = "SA";
					String guildName = "OnlyFlans";
					String urlGuild = url + "guildName=" + guildName + "&" + "region=" + region;

					System.out.println(urlGuild);

					try {
						Document document = Jsoup.connect(urlGuild).get();

						// Seleccionar todos los elementos li en la ruta dada
						Elements liElements = document.select("#wrap > div > div.container.guild_profile > article > div.box_list_area > ul > li");

						// Iterar sobre cada elemento li
						for (Element liElement : liElements) {
							// Seleccionar el primer y segundo div dentro de li
							Element div1 = liElement.selectFirst("div:nth-child(1) > span > span.text > a");
							Element div2 = liElement.selectFirst("div:nth-child(2) > span > span.text > a");

							// Obtener y mostrar los nombres
							String name1 = (div1 != null) ? div1.text() : null;
							String name2 = (div2 != null) ? div2.text() : null;

							// Verificar si el nickname actual contiene alguno de los nombres de la lista
							if (nickname != null && nickname.contains(name1) || nickname.contains(name2)) {
								System.out.println("Usuario encontrado en la lista. Nombre: " + nickname + " ID: " + userId);
								Client existClient = clientRepository.findByDiscordId(userId);
								if (existClient == null) {
									Client newClient = new Client(nickname, userId);
									clientRepository.save(newClient);
									System.out.println("Usuario creado con éxito.");
									channel.createMessage("Usuario encontrado en la guild y creado con éxito.").block();
									break;
								} else {
									System.out.println("El ID pertenece a un usuario existente.");
									channel.createMessage("El ID pertenece a un usuario existente.").block();
									break;
								}
							} else {
								System.out.println("Usuario no encontrado");
							}

							if (name1 == null || name2 == null) {
								System.out.println("No hay más miembros.");
								channel.createMessage("Usuario no encontrado.").block();
								break; // Termina el bucle si no hay más miembros
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			gateway.onDisconnect().block();
		};
	}
}
