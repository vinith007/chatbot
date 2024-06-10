package com.chat.bot.constants;

public enum ChatMessages {

    INVALID_RESPONSE("I'm sorry, I didn't understand your response. Return to the last request."),
    INVALID_RESPONSE_NAME("Invalid Message"),
    END_OPTIONS("Do you want to restart the chat? Please respond with one of the following options:\n1: Restart conversation by clearing chat history\n2: Continue chat again from the first node with previous history\n3: Thanks for chatting"),
    THANKS_MESSAGE("Thanks for chatting!"),

    USER("User"),
    YOU("You: "),
    CHATBOT("Chatbot");

    private final String message;

    ChatMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
