package sample.context.orm;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;

import jakarta.persistence.EntityManager;

/**
 * JPA 関連のユーティリティを提供します。
 */
public class JpaUtils {
    /** 指定したクラスのエンティティ情報を返します ( ID 概念含む ) */
    @SuppressWarnings("unchecked")
    public static <T> JpaEntityInformation<T, Object> entityInformation(EntityManager em, Class<?> clazz) {
        return (JpaEntityInformation<T, Object>) JpaEntityInformationSupport.getEntityInformation(clazz, em);
    }
}
