package opyta.bdo.bdoBot.repositories;

import opyta.bdo.bdoBot.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client,Long> {
    Client findByDiscordId(Long discordId);
}
