package org.example.championship;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChartVisualizerFullTest {

    private IResolver resolverMock;
    private DataMapper mapper;
    private ChartVisualizer visualizer;

    @BeforeEach
    void setUp() {
        resolverMock = mock(IResolver.class);
        mapper = new DataMapper(resolverMock);
        visualizer = new ChartVisualizer(mapper);
    }

    /* -------------------------------------------------
       1. Приватный метод createDataset() – уже проверен.
       ------------------------------------------------- */
    // (тест остался прежним, см. предыдущий ответ)

    /* -------------------------------------------------
       2. Тестируем initUI() через рефлексию.
       ------------------------------------------------- */
    @Test
    void testInitUICreatesChartPanel() throws Exception {
        // Сначала подготавливаем данные, чтобы createDataset() вернул что‑то
        when(resolverMock.getPlayers()).thenReturn(
                List.of(
                        new Player("A","TeamX","C","FORWARD","DE","A",12_000_000L,10,5,2,0,0),
                        new Player("B","TeamY","C","DEFENDER","DE","B",7_500_000L,12,8,3,0,0)
                )
        );

        // Вызываем приватный метод initUI()
        Method initUi = ChartVisualizer.class.getDeclaredMethod("initUI");
        initUi.setAccessible(true);
        initUi.invoke(visualizer);

        // После initUI() в объекте должна быть панель с графиком
        // Доступ к ней получим через рефлексию (поле chartPanel в ChartVisualizer – private)
        java.lang.reflect.Field field = ChartVisualizer.class.getDeclaredField("chartPanel");
        field.setAccessible(true);
        Object panelObj = field.get(visualizer);
        assertNotNull(panelObj, "ChartPanel должен быть создан");

        ChartPanel chartPanel = (ChartPanel) panelObj;
        JFreeChart chart = chartPanel.getChart();
        assertNotNull(chart, "JFreeChart внутри ChartPanel не должен быть null");
    }

    /* -------------------------------------------------
       3. Тест main() – уже покрыт (assertDoesNotThrow).
       ------------------------------------------------- */
}
