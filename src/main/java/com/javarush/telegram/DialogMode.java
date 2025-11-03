package com.javarush.telegram;


public enum DialogMode {
    MAIN,
    CV,         // генерация резюме
    COVER,      // сопроводительное
    INTERVIEW,  // практика интервью
    SUPPORT,    // психо-поддержка
    GPT         // свободные вопросы
}