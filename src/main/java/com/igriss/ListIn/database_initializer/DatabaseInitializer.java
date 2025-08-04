package com.igriss.ListIn.database_initializer;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.igriss.ListIn.chat.repository.ChatMessageRepository;
import com.igriss.ListIn.chat.repository.ChatRoomRepository;
import com.igriss.ListIn.comment.repository.CommentRepository;
import com.igriss.ListIn.location.dto.LocationDTO;
import com.igriss.ListIn.location.entity.Country;
import com.igriss.ListIn.location.service.LocationService;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.repository.PublicationRepository;
import com.igriss.ListIn.security.roles.Role;
import com.igriss.ListIn.user.entity.User;
import com.igriss.ListIn.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;
    private final ElasticsearchClient elasticsearchClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocationService locationService;
    private final PublicationRepository publicationRepository;
    private final CommentRepository repository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Value("${elasticsearch.index-name}")
    private String indexName;

    private final List<String> scripts = List.of(
            "/database_sql_scripts/categories.sql",
            "/database_sql_scripts/attribute_keys.sql",
            "/database_sql_scripts/attribute_values.sql",
            "/database_sql_scripts/auto/attribute_keys.sql",
            "/database_sql_scripts/auto/attribute_values.sql",
            "/database_sql_scripts/real_estate/attribute_keys.sql",
            "/database_sql_scripts/real_estate/attribute_values.sql",
            "/database_sql_scripts/clothes/attribute_keys.sql",
            "/database_sql_scripts/clothes/attribute_values.sql",
            "/database_sql_scripts/home_&_garden/attribute_keys.sql",
            "/database_sql_scripts/home_&_garden/attribute_values.sql",
            "/database_sql_scripts/beauty_&_health/attribute_keys.sql",
            "/database_sql_scripts/beauty_&_health/attribute_values.sql",
            "/database_sql_scripts/luxurious_accessories/attribute_keys.sql",
            "/database_sql_scripts/luxurious_accessories/attribute_values.sql",
            "/database_sql_scripts/flowers_&_gifts/attribute_keys.sql",
            "/database_sql_scripts/flowers_&_gifts/attribute_values.sql",
            "/database_sql_scripts/animals/attribute_keys.sql",
            "/database_sql_scripts/animals/attribute_values.sql",
            "/database_sql_scripts/category_attributes.sql",


            "/database_sql_scripts/models/auto/motorcycle_brand_models.sql",
            "/database_sql_scripts/models/auto/car_brand_models.sql",
            "/database_sql_scripts/models/auto/commercial_vehicle_brand_models.sql",
            "/database_sql_scripts/models/auto/electric_vehicle_brand_models.sql",
            "/database_sql_scripts/models/auto/watercraft_brand_models.sql",
            "/database_sql_scripts/models/auto/special_vehicle_brand_models.sql",
            "/database_sql_scripts/models/auto/agricultural_&_construction_vehicle_brand_models.sql",
            "/database_sql_scripts/models/auto/vehicle_rental_brand_models.sql",
            "/database_sql_scripts/models/electronics/smartphone_brand_models.sql",
            "/database_sql_scripts/models/electronics/laptop_brand_models.sql",
            "/database_sql_scripts/models/electronics/smartwatch_brand_models.sql",
            "/database_sql_scripts/models/electronics/tablet_brand_models.sql",
            "/database_sql_scripts/models/electronics/console_brand_models.sql",
            "/database_sql_scripts/models/electronics/laptop_processor_models.sql",
            "/database_sql_scripts/models/electronics/pc_processor_models.sql",
            "/database_sql_scripts/models/electronics/laptop_gpu_models.sql",
            "/database_sql_scripts/models/electronics/pc_gpu_models.sql",
            "/database_sql_scripts/models/electronics/pc_brand_models.sql",
            "/database_sql_scripts/models/clothes/sizes.sql",

            "/database_sql_scripts/numerics/auto_numeric_fields.sql",
            "/database_sql_scripts/numerics/real_estate_numeric_fields.sql",

            "/database_sql_scripts/location-tree/countries.sql",
            "/database_sql_scripts/location-tree/states.sql",
            "/database_sql_scripts/location-tree/counties.sql"
    );

    @PostConstruct //todo -> to be removed before next use
    public void delete() {
        chatRoomRepository.deleteAll();
    }

//  @PostConstruct
//    public void flushRedis() {
//        Objects.requireNonNull(redisTemplate
//                        .getConnectionFactory()
//                )
//                .getConnection()
//                .serverCommands()
//                .flushAll();
//        log.info("#Redis cache successfully cleared");
//    }
//
//
//    @PostConstruct
//    public void init() {
//        clearDatabase();
//        for (String script : scripts) {
//            executeScript(script);
//        }
//
//        LocationDTO locationDTO = locationService.getLocation("Узбекистан", "Ташкент", "Яккасарай", "ru");
//        userRepository.saveAll(
//                List.of(
//                        User.builder().nickName("Davron").enableCalling(true).phoneNumber("+998 90 000 00 09").email("d.no_replay@listin.uz").biography("Admin")
//                                .password(passwordEncoder.encode("string")).role(Role.ADMIN).isGrantedForPreciseLocation(true)
//                                .country(locationDTO.getCountry())
//                                .county(locationDTO.getCounty())
//                                .country(locationDTO.getCountry())
//                                .state(locationDTO.getState())
//                                .locationName("Tashkent").longitude(1234.1234).latitude(-43.234234).build(),
//                        User.builder().nickName("Qobil").enableCalling(true).phoneNumber("+998 90 000 00 09").email("q.no_replay@listin.uz").biography("Admin")
//                                .password(passwordEncoder.encode("string")).role(Role.ADMIN).isGrantedForPreciseLocation(true).locationName("Tashkent")
//                                .country(locationDTO.getCountry())
//                                .county(locationDTO.getCounty())
//                                .state(locationDTO.getState())
//                                .country(locationDTO.getCountry())
//                                .longitude(1234.1234).latitude(-43.234234).build(),
//                        User.builder().nickName("Abdulaxad").enableCalling(true).phoneNumber("+998 90 000 00 09").email("a.no_replay@listin.uz").biography("Admin")
//                                .password(passwordEncoder.encode("string")).role(Role.ADMIN).isGrantedForPreciseLocation(true).locationName("Tashkent")
//                                .country(locationDTO.getCountry())
//                                .county(locationDTO.getCounty())
//                                .state(locationDTO.getState())
//                                .country(locationDTO.getCountry())
//                                .longitude(1234.1234).latitude(-43.234234).build()
//                )
//        );
//    }
//
//    private void clearDatabase() {
//        try {
//            List<String> tablesToClear = List.of(
//                    "category_attributes",
//                    "attribute_values",
//                    "attribute_keys",
//                    "categories",
//                    "smartphone_brand_models",
//                    "laptop_brand_models",
//                    "smartwatch_brand_models",
//                    "tablet_brand_models",
//                    "console_brand_models"
//            );
//
//            for (String table : tablesToClear) {
//                jdbcTemplate.update("DELETE FROM " + table);
//            }
//            log.info("#Database cleared successfully.");
//        } catch (Exception e) {
//            log.error("#Error while clearing the database: {}", e.getMessage());
//        }
//    }
//
//    private void executeScript(String scriptPath) {
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
//                Objects.requireNonNull(getClass().getResourceAsStream(scriptPath)), StandardCharsets.UTF_8))) {
//            StringBuilder sql = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                sql.append(line).append("\n");
//            }
//            jdbcTemplate.execute(sql.toString());
//        } catch (Exception e) {
//            log.error("#Error executing script {}: {}", scriptPath, e.getMessage());
//        }
//    }
//
//
//    @PostConstruct
//    public void clearElasticsearchData() {
//        try {
//            if (elasticsearchClient.indices().exists(e -> e.index(indexName)).value()) {
//                elasticsearchClient.indices().delete(d -> d.index(indexName));
//                log.info("#Index deleted: {}", indexName);
//            }
//        } catch (Exception e) {
//            log.error("#Exception while clearing elastic search data: {}", e.getMessage());
//        }
//    }
}






