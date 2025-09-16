package com.example.essaycorrect.data.model;

import java.util.List;

public class AIRequest {
    private String model;
    private double temperature;
    private List<AIMessage> messages;
    private String character;

    public AIRequest() {}

    public AIRequest(String model, double temperature, List<AIMessage> messages, String character) {
        this.model = model;
        this.temperature = temperature;
        this.messages = messages;
        this.character = character;
    }

    public String getModel() { 
        return model; 
    }
    
    public void setModel(String model) { 
        this.model = model; 
    }

    public double getTemperature() { 
        return temperature; 
    }
    
    public void setTemperature(double temperature) { 
        this.temperature = temperature; 
    }

    public List<AIMessage> getMessages() { 
        return messages; 
    }
    
    public void setMessages(List<AIMessage> messages) { 
        this.messages = messages; 
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public static class AIMessage {
        private String role;
        private String content;

        public AIMessage() {}

        public AIMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { 
            return role; 
        }
        
        public void setRole(String role) { 
            this.role = role; 
        }

        public String getContent() { 
            return content; 
        }
        
        public void setContent(String content) { 
            this.content = content; 
        }
    }
}