package ru.shift.view;

import javax.swing.*;
import java.awt.*;

public class AboutWindow extends JDialog {
    public AboutWindow(JFrame owner) {
        super(owner, "About", true);
        JTextArea ta = new JTextArea("""
                Сапёр по MVC.
                Клик ЛКМ: открытие клетки
                Клик ПКМ: поставить/убрать флажок
                Клик средней кнопки мышки: открыть клетки, если флажков столько же, сколько цифра
                Первый клик всегда безопасный
                """);
        ta.setEditable(false);
        ta.setOpaque(false);
        add(new JScrollPane(ta));
        setSize(450, 180);
        setLocationRelativeTo(owner);
    }
}

