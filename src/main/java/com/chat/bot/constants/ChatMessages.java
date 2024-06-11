package com.chat.bot.constants;

/**
 * Enum representing various chat messages used in the chatbot application.
 */
public enum ChatMessages {

    INVALID_MESSAGE("I'm sorry, I didn't understand your response. Return to the last request."),
    INVALID_MESSAGE_NAME("Invalid Message"),

    END_MESSAGE("Thanks for chatting!!"),
    END_MESSAGE_NAME("End Message"),
    USER("User"),
    YOU("You: "),
    CHATBOT("Chatbot");

    /**
     * The message associated with the enum value.
     */
    private final String message;

    /**
     * Constructor to initialize the enum value with the specified message.
     *
     * @param message the message associated with the enum value
     */
    ChatMessages(String message) {
        this.message = message;
    }

    /**
     * Retrieves the message associated with the enum value.
     *
     * @return the message associated with the enum value
     */
    public String getMessage() {
        return message;
    }
}
