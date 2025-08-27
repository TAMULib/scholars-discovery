package edu.tamu.scholars.middleware.export.resolver;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import edu.tamu.scholars.middleware.export.argument.ExportArg;

/**
 * 
 */
public final class ExportArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String EXPORT_QUERY_PARAM_KEY = "export";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        ResolvableType resolvableType = ResolvableType.forMethodParameter(parameter);
        return resolvableType.hasGenerics() && ExportArg.class.isAssignableFrom(resolvableType.getGeneric(0).resolve());
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        @Nullable ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        @Nullable WebDataBinderFactory binderFactory
    ) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        return Collections.list(request.getParameterNames()).stream()
            .filter(paramName -> paramName.equals(EXPORT_QUERY_PARAM_KEY))
            .map(request::getParameterValues)
            .map(Arrays::asList)
            .flatMap(Collection::stream)
            .map(ExportArg::of)
            .toList();
    }

}
