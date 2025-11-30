package com.yamahamba.careerbro;

public class UserMemory {

    // Структурная часть — используем уже существующий UserInfo
    public UserInfo profile = new UserInfo();

    // Текстовые заметки из разных режимов
    public String lastSupportNote;       // что человек писал в /support
    public String lastDirectionNote;     // что он размышлял в /direction
    public String lastNavigatorQuestion; // о чём он спрашивал в /navigator
    public String lastInterviewGoal;     // какой у него был фокус в /interview
    public String lastMainNote;          // что он писал в главном режиме (MAIN)

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Профиль
        sb.append("\"profession\":\"").append(escape(profile.profession)).append("\",");
        sb.append("\"experienceYears\":\"").append(escape(profile.experienceYears)).append("\",");
        sb.append("\"keySkills\":\"").append(escape(profile.keySkills)).append("\",");
        sb.append("\"achievements\":\"").append(escape(profile.achievements)).append("\",");
        sb.append("\"jobTarget\":\"").append(escape(profile.jobTarget)).append("\",");

        // Заметки
        sb.append("\"lastSupportNote\":\"").append(escape(lastSupportNote)).append("\",");
        sb.append("\"lastDirectionNote\":\"").append(escape(lastDirectionNote)).append("\",");
        sb.append("\"lastNavigatorQuestion\":\"").append(escape(lastNavigatorQuestion)).append("\",");
        sb.append("\"lastInterviewGoal\":\"").append(escape(lastInterviewGoal)).append("\",");
        sb.append("\"lastMainNote\":\"").append(escape(lastMainNote)).append("\"");

        sb.append("}");
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}