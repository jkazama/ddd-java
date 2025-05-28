package sample.context;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import sample.util.Warns.Warn;

/**
 * Retrieves strings from message resources based on keys.
 */
public interface MessageAccessor {

    /**
     * Retrieves a message with the default locale.
     * <p>
     * Returns the key as-is when no matching key is found.
     * </p>
     *
     * @param messageKey  message key
     * @param messageArgs message arguments
     * @return message
     */
    default String load(String messageKey, String... messageArgs) {
        return this.load(Locale.getDefault(), messageKey, messageArgs);
    }

    /**
     * Retrieves a message with the specified locale.
     * <p>
     * Returns the key as-is when no matching key is found.
     * </p>
     *
     * @param locale      locale
     * @param messageKey  message key
     * @param messageArgs message arguments
     * @return message
     */
    default String load(Locale locale, String messageKey, String... messageArgs) {
        return this.load(locale, messageKey, messageArgs == null ? List.of() : List.of(messageArgs));
    }

    /**
     * Retrieves a message with the default locale.
     * <p>
     * Returns the key as-is when no matching key is found.
     * </p>
     *
     * @param messageKey  message key
     * @param messageArgs message arguments
     * @return message
     */
    default String load(String messageKey, List<String> messageArgs) {
        return this.load(Locale.getDefault(), messageKey, messageArgs);
    }

    /**
     * Retrieves a message with the specified locale.
     * <p>
     * Returns the key as-is when no matching key is found.
     * </p>
     *
     * @param locale      locale
     * @param messageKey  message key
     * @param messageArgs message arguments
     * @return message
     */
    String load(Locale locale, String messageKey, List<String> messageArgs);

    /**
     * Retrieves the message for the specified validation exception.
     * <p>
     * Returns the validation exception message as-is when no message exists for the
     * validation exception.
     * </p>
     *
     * @param locale locale
     * @param warn   validation exception
     * @return message
     */
    String load(Locale locale, Warn warn);

    /**
     * Retrieves the message for the specified validation exception.
     * <p>
     * Returns the validation exception message as-is when no message exists for the
     * validation exception.
     * </p>
     *
     * @param warn validation exception
     * @return message
     */
    default String load(Warn warn) {
        return this.load(Locale.getDefault(), warn);
    }

    /** Standard implementation of MessageAccessor. */
    @Component
    @RequiredArgsConstructor(staticName = "of")
    public static class MessageAccessorImpl implements MessageAccessor {
        private final MessageSource msg;

        /** {@inheritDoc} */
        @Override
        public String load(Locale locale, String messageKey, List<String> messageArgs) {
            return this.msg.getMessage(messageKey, messageArgs.toArray(String[]::new), messageKey, locale);
        }

        /** {@inheritDoc} */
        @Override
        public String load(Locale locale, Warn warn) {
            return this.load(locale, warn.message(), warn.messageArgs());
        }
    }

    /** Mock implementation of MessageAccessor. */
    public static class MessageAccessorMock implements MessageAccessor {
        private final StaticMessageSource msgOrigin = new StaticMessageSource();

        /** {@inheritDoc} */
        public String load(Locale locale, String messageKey, List<String> messageArgs) {
            return this.msg().load(locale, messageKey, messageArgs);
        }

        private MessageAccessor msg() {
            return MessageAccessorImpl.of(this.msgOrigin);
        }

        /** {@inheritDoc} */
        public String load(Locale locale, Warn warn) {
            return this.msg().load(locale, warn);
        }

        public MessageAccessorMock put(String messageKey, String message) {
            this.msgOrigin.addMessage(messageKey, Locale.getDefault(), message);
            return this;
        }

        public MessageAccessorMock put(String messageKey, String message, Locale locale) {
            this.msgOrigin.addMessage(messageKey, locale, message);
            return this;
        }

    }

}