import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
class EpicTest {
    @Test
     void testEquals_sameId_shouldBeEqual() {
        Epic epic1 = new Epic("Имя", "Описание");
        epic1.setId(1);
        Epic epic2 = new Epic("Другое имя", "Другое описание");
        epic2.setId(1);
        assertEquals(epic1, epic2);
    }
}