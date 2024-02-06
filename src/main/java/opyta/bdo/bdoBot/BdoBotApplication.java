package opyta.bdo.bdoBot;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
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
	public CommandLineRunner initData() {
		return args -> {

			DiscordClient client = DiscordClient.create(token);
			GatewayDiscordClient gateway = client.login().block();

			System.out.println("Ready to read commands");

			gateway.on(MessageCreateEvent.class).subscribe(event -> {
				Message message = event.getMessage();
				if ("!ping".equals(message.getContent())) {
					MessageChannel channel = message.getChannel().block();

					String region = "SA";
					String guildName = "OnlyFlans";
					String urlGuild = url + "guildName=" + guildName +"&"+ "region=" + region;

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

							if (name1 == null || name2 == null) {
								System.out.println("No hay mas miembros.");
								break; // Termina el bucle si no hay más miembros
							}

							System.out.println("Familia: " + name1);
							System.out.println("Familia: " + name2);
						}
						channel.createMessage("Pong!").block();

					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			});

			gateway.onDisconnect().block();
		};
	}
}
