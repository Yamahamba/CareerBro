package com.javarush.telegram;

public class UserInfo {
    // Для карьерного режима
    public String profession;       // Кто вы по профессии
    public String experienceYears;  // Сколько лет опыта
    public String keySkills;        // Ключевые навыки
    public String achievements;     // Достижения
    public String jobTarget;        // Цель / желаемая позиция

    // Оставим для совместимости, если нужно будет использовать старый Tinder-режим
    public String name;
    public String sex;
    public String age;
    public String city;
    public String occupation;
    public String hobby;
    public String handsome;
    public String wealth;
    public String annoys;
    public String goals;

    private String fieldToString(String str, String description) {
        if (str != null && !str.isEmpty())
            return description + ": " + str + "\n";
        else
            return "";
    }

    @Override
    public String toString() {
        String result = "";

        result += fieldToString(profession, "Профессия");
        result += fieldToString(experienceYears, "Опыт");
        result += fieldToString(keySkills, "Навыки");
        result += fieldToString(achievements, "Достижения");
        result += fieldToString(jobTarget, "Цель");

        // Можно раскомментировать ниже, если используешь старые поля
        // result += fieldToString(name, "Имя");
        // result += fieldToString(sex, "Пол");
        // result += fieldToString(age, "Возраст");
        // result += fieldToString(city, "Город");
        // result += fieldToString(occupation, "Профессия");
        // result += fieldToString(hobby, "Хобби");
        // result += fieldToString(handsome, "Красота");
        // result += fieldToString(wealth, "Доход");
        // result += fieldToString(annoys, "Раздражает");
        // result += fieldToString(goals, "Цели");

        return result;
    }
}