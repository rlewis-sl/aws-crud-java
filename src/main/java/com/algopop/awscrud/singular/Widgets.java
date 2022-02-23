package com.algopop.awscrud.singular;

import java.util.List;

import com.algopop.awscrud.ItemNotFoundException;
import com.algopop.awscrud.model.Widget;
import com.algopop.awscrud.model.WidgetCollection;

public class Widgets {
    private static final String WIDGET_ID = "123456789";
    private static final String WIDGET_NAME = "SINGULAR";
    private static final Float WIDGET_COST = 1.0f;
    private static final Float WIDGET_WEIGHT = 1.0f;

    private Widgets() {}

    public static String singularId() {
        return WIDGET_ID;
    }

    public static Widget singular() {return new Widget(WIDGET_ID, WIDGET_NAME, WIDGET_COST, WIDGET_WEIGHT);}

    public static WidgetCollection getWidgets() {
        return new WidgetCollection(List.of(singular()));
    }

    public static Widget getWidget(String id) throws ItemNotFoundException {
        return getWidget(id, false);
    }

    public static Widget getWidget(String id, boolean consistency) {
        return singular();
    }

    public static String createWidget(Widget widget) {
       return WIDGET_ID;
    }

    public static Widget updateWidget(Widget widget) {
        return singular();
    }

    public static void deleteWidget(String id) {}

    public static Widget empty() {
        return new Widget("", "", null, null);
    }
}
