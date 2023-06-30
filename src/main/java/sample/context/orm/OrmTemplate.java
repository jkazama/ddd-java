package sample.context.orm;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import sample.context.ValidationException;
import sample.model.DomainErrorKeys;

/**
 * Simple Accessor for EntityManager of JPA.
 * (you are formed every session, and please use it)
 * <p>
 * If there is the handling of that You want to use by a method EntityManager,
 * add a wrap method as needed.
 */
@RequiredArgsConstructor(staticName = "of")
public class OrmTemplate {
    private final EntityManager em;

    public <T> Optional<T> get(final String qlString, final Object... args) {
        List<T> list = find(qlString, args);
        return list.stream().findFirst();
    }

    public <T> T load(final String qlString, final Object... args) {
        Optional<T> v = get(qlString, args);
        return v.orElseThrow(() -> new ValidationException(DomainErrorKeys.ENTITY_NOT_FOUND));
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> find(final String qlString, final Object... args) {
        return bindArgs(em.createQuery(qlString), args).getResultList();
    }

    @SuppressWarnings("unchecked")
    public <T> Page<T> find(final String qlString, final Pageable page, final Object... args) {
        @SuppressWarnings("deprecation")
        long total = load(QueryUtils.createCountQueryFor(qlString), args);
        List<T> list = bindArgs(em.createQuery(qlString), page, args).getResultList();
        return new PageImpl<>(list, page, total);
    }

    public <T> Optional<T> getNamed(final String name, final Object... args) {
        List<T> list = findNamed(name, args);
        return list.stream().findFirst();
    }

    public <T> T loadNamed(final String name, final Object... args) {
        Optional<T> v = getNamed(name, args);
        return v.orElseThrow(() -> new ValidationException(DomainErrorKeys.ENTITY_NOT_FOUND));
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findNamed(final String name, final Object... args) {
        return bindArgs(em.createNamedQuery(name), args).getResultList();
    }

    @SuppressWarnings("unchecked")
    public <T> Page<T> findNamed(
            String name,
            String nameCount,
            final Pageable page,
            final Map<String, Object> args) {
        long total = loadNamed(nameCount, args);
        List<T> list = bindArgs(em.createNamedQuery(name), page, args).getResultList();
        return new PageImpl<>(list, page, total);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findBySql(final String sql, final Object... args) {
        return bindArgs(em.createNativeQuery(sql), args).getResultList();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findBySql(String sql, Class<T> clazz, final Object... args) {
        return bindArgs(em.createNativeQuery(sql, clazz), args).getResultList();
    }

    @SuppressWarnings("unchecked")
    public <T> Page<T> findBySql(
            String sql,
            String sqlCount,
            final Pageable page,
            final Object... args) {
        long total = findBySql(sqlCount, args).stream()
                .findFirst()
                .map(v -> Long.parseLong(v.toString()))
                .orElse(0L);
        return new PageImpl<>(
                bindArgs(em.createNativeQuery(sql), page, args).getResultList(),
                page,
                total);
    }

    @SuppressWarnings("unchecked")
    public <T> Page<T> findBySql(
            String sql,
            String sqlCount,
            Class<T> clazz,
            final Pageable page,
            final Object... args) {
        long total = findBySql(sqlCount, args).stream()
                .findFirst()
                .map(v -> Long.parseLong(v.toString()))
                .orElse(0L);
        return new PageImpl<>(
                bindArgs(em.createNativeQuery(sql, clazz), page, args).getResultList(),
                page,
                total);
    }

    public int execute(String qlString, final Object... args) {
        return bindArgs(em.createQuery(qlString), args).executeUpdate();
    }

    public int executeNamed(String name, final Object... args) {
        return bindArgs(em.createNamedQuery(name), args).executeUpdate();
    }

    public int executeSql(String sql, final Object... args) {
        return bindArgs(em.createNativeQuery(sql), args).executeUpdate();
    }

    public void callStoredProcedure(String procedureName, Consumer<StoredProcedureQuery> proc) {
        proc.accept((StoredProcedureQuery) bindArgs(em.createStoredProcedureQuery(procedureName)));
    }

    public Query bindArgs(final Query query, final Object... args) {
        return bindArgs(query, null, args);
    }

    public Query bindArgs(final Query query, final Pageable page, final Object... args) {
        Optional.ofNullable(page).ifPresent((pg) -> {
            if (page.getPageNumber() > 0) {
                query.setFirstResult((int) page.getOffset());
            }
            if (page.getPageSize() > 0) {
                query.setMaxResults(page.getPageSize());
            }
        });
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> argNamed = (Map<String, Object>) arg;
                    argNamed.forEach((k, v) -> query.setParameter(k, v));
                } else {
                    query.setParameter(i + 1, args[i]);
                }
            }
        }
        return query;
    }

}
