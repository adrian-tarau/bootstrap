package net.microfalx.bootstrap.search;

import lombok.Getter;
import lombok.Setter;
import net.datafaker.Faker;
import net.datafaker.providers.base.Shakespeare;
import net.microfalx.lang.IdGenerator;
import net.microfalx.resource.MemoryResource;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Setter
@Getter
class DataGenerator {

    static final String EQUIPMENT_FIELD = "equipment";
    static final String CAR_FIELD = "car";
    static final String WATCH_FIELD = "watch";
    static final String PRODUCT_FIELD = "product";
    static final String DEPARTMENT_FIELD = "department";

    private final IndexService indexService;
    private final Faker faker = new Faker();
    private final Random random = ThreadLocalRandom.current();
    private final Map<String, Set<String>> attributes = new HashMap<>();

    private int documentCount = 100;
    private Duration timeRange = Duration.ofHours(24);


    DataGenerator(IndexService indexService) {
        this.indexService = indexService;
    }

    void reset() {
        indexService.clear();
        attributes.clear();
    }

    Set<String> getAttribute(String name) {
        return attributes.getOrDefault(name, Collections.emptySet());
    }

    Set<String> execute() {
        Set<String> ids = new HashSet<>();
        ZonedDateTime dateTime = ZonedDateTime.now().minusMinutes(timeRange.toMinutes());
        int step = (int) (timeRange.toMinutes() / documentCount);
        for (int i = 0; i < documentCount; i++) {
            String id = IdGenerator.get().nextAsString();
            ids.add(id);
            Document document = new Document(id, faker.book().title());
            update(document, dateTime);
            indexService.index(document, false);
            dateTime = dateTime.plusMinutes(step);
        }
        return ids;
    }

    private void update(Document document, ZonedDateTime dateTime) {
        document.setCreatedAt(dateTime);
        if (random.nextFloat() > 0.5) document.setModifiedAt(dateTime.plusMinutes(random.nextInt(10)));
        updateCoreAttributes(document);
        updateAttributes(document);
        updateTags(document);
        document.setBody(MemoryResource.create(getRandomText()));
        document.setDescription(getNextSentence());
    }

    private void updateCoreAttributes(Document document) {
        document.setOwner(faker.company().name());
        document.setType(faker.company().industry());
        addAttribute(Document.OWNER_FIELD, document.getOwner());
        addAttribute(Document.TYPE_FIELD, document.getType());
    }

    private void updateAttributes(Document document) {
        if (random.nextFloat() > 0.9) {
            add(document, EQUIPMENT_FIELD, faker.appliance().equipment());
        }
        if (random.nextFloat() > 0.8) {
            add(document, CAR_FIELD, faker.brand().car());
        }
        if (random.nextFloat() > 0.7) {
            add(document, WATCH_FIELD, faker.brand().watch());
        }
        if (random.nextFloat() > 0.6) {
            add(document, PRODUCT_FIELD, faker.commerce().productName());
        }
        if (random.nextFloat() > 0.6) {
            add(document, DEPARTMENT_FIELD, faker.commerce().department());
        }
    }

    private String getRandomText() {
        StringBuilder builder = new StringBuilder();
        int paragraphCount = 3 + random.nextInt(20);
        for (int i = 0; i < paragraphCount; i++) {
            int sentenceCount = 1 + random.nextInt(5);
            for (int j = 0; j < sentenceCount; j++) {
                builder.append(getNextSentence());
            }
            if (i < (paragraphCount - 1)) builder.append("\n");
        }
        return builder.toString();
    }

    private String getNextSentence() {
        Shakespeare shakespeare = faker.shakespeare();
        float value = random.nextFloat();
        if (value > 0.9) {
            return faker.greekPhilosopher().quote();
        } else if (value > 0.8) {
            return faker.gameOfThrones().quote();
        } else if (value > 0.7) {
            return faker.freshPrinceOfBelAir().quotes();
        } else if (value > 0.6) {
            return shakespeare.asYouLikeItQuote();
        } else if (value > 0.5) {
            return shakespeare.hamletQuote();
        } else if (value > 0.4) {
            return shakespeare.kingRichardIIIQuote();
        } else if (value > 0.3) {
            return shakespeare.kingRichardIIIQuote();
        } else if (value > 0.2) {
            return faker.heyArnold().quotes();
        } else {
            return faker.futurama().quote();
        }
    }

    private void add(Document document, String attributeName, String attributeValue) {
        document.add(attributeName, attributeValue);

    }

    private void addAttribute(String attributeName, String attributeValue) {
        attributes.computeIfAbsent(attributeName, s -> new HashSet<>()).add(attributeValue);
    }

    private void updateTags(Document document) {
        document.addTag("tag1");
        if (random.nextFloat() > 0.5) {
            document.addTag("tag2");
        }
    }


}
