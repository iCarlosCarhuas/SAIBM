package edu.pe.cibertec.SAIBM;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

class WebRouteInventoryContractTests {

    private static final String CONTROLLER_PACKAGE = "edu.pe.cibertec.SAIBM.controller";

    private static final Set<String> EXPECTED_ROUTES = Set.of(
            "GET / -> IndexController#index",
            "GET /biblioteca/editar/{id} -> LibroController#editarLibro",
            "POST /biblioteca/eliminar/{id} -> LibroController#eliminarLibro",
            "POST /biblioteca/guardar -> LibroController#guardarLibro",
            "GET /biblioteca/listar_libros -> LibroController#listarLibros",
            "GET /biblioteca/nuevo -> LibroController#nuevoLibro",
            "GET /generar/pdf/{usuarioId} -> ReportController#generarPDF",
            "GET /generar/reporteLibros -> ReportController#generarReporteLibros",
            "GET /index -> IndexController#index",
            "POST /reserva/agregar -> ReservaController#agregarReserva",
            "POST /reserva/cancelar -> ReservaController#cancelarReserva",
            "POST /reserva/confirmar -> ReservaController#confirmarReservas",
            "POST /reserva/eliminar/{id} -> ReservaController#eliminarReserva",
            "GET /reserva/mantener_reservas -> ReservaController#mantenerReservas",
            "GET /reserva/mis_reservas -> ReservaController#misReservas",
            "GET /security/login -> SecurityController#mostrarLogin",
            "POST /security/login -> SecurityController#procesarLogueo",
            "GET /security/logout -> SecurityController#cerrarSesion",
            "GET /security/register -> SecurityController#mostrarRegistro",
            "POST /security/register -> SecurityController#crear",
            "GET /usuario/editar/{id} -> UsuarioController#mostrarFormularioEditar",
            "POST /usuario/eliminar/{id} -> UsuarioController#eliminarUsuario",
            "POST /usuario/guardar -> UsuarioController#guardarUsuario",
            "GET /usuario/listar_usuarios -> UsuarioController#listarUsuarios",
            "GET /usuario/nuevo -> UsuarioController#mostrarFormularioRegistro"
    );

    @Test
    void mvcRouteAnnotationsMatchTheDocumentedLegacyContract() throws Exception {
        Set<String> actualRoutes = discoverRoutes();
        if (!actualRoutes.equals(EXPECTED_ROUTES)) {
            Set<String> missing = new TreeSet<>(EXPECTED_ROUTES);
            missing.removeAll(actualRoutes);
            Set<String> unexpected = new TreeSet<>(actualRoutes);
            unexpected.removeAll(EXPECTED_ROUTES);
            fail("Legacy MVC route contract changed. Update this contract and "
                    + "docs/migration/legacy-route-inventory.md intentionally."
                    + "\nMissing routes: " + missing
                    + "\nUnexpected routes: " + unexpected
                    + "\nExpected count: " + EXPECTED_ROUTES.size()
                    + "\nActual count: " + actualRoutes.size());
        }
    }

    private Set<String> discoverRoutes() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        CachingMetadataReaderFactory readers = new CachingMetadataReaderFactory(resolver);
        String pattern = "classpath*:" + CONTROLLER_PACKAGE.replace('.', '/') + "/**/*.class";
        Set<String> routes = new TreeSet<>();

        for (Resource resource : resolver.getResources(pattern)) {
            MetadataReader metadata = readers.getMetadataReader(resource);
            Class<?> type = Class.forName(metadata.getClassMetadata().getClassName());
            if (!type.isAnnotationPresent(Controller.class)) {
                continue;
            }

            RequestMapping classMapping = findMergedAnnotation(type, RequestMapping.class);
            String[] prefixes = paths(classMapping);
            for (Method method : type.getDeclaredMethods()) {
                RequestMapping mapping = findMergedAnnotation(method, RequestMapping.class);
                if (mapping == null) {
                    continue;
                }
                for (String prefix : prefixes) {
                    for (String path : paths(mapping)) {
                        for (RequestMethod httpMethod : methods(mapping)) {
                            routes.add(httpMethod.name() + " " + join(prefix, path) + " -> "
                                    + type.getSimpleName() + "#" + method.getName());
                        }
                    }
                }
            }
        }
        return routes;
    }

    private String[] paths(RequestMapping mapping) {
        if (mapping == null || mapping.path().length == 0) {
            return new String[]{""};
        }
        return mapping.path();
    }

    private RequestMethod[] methods(RequestMapping mapping) {
        return mapping.method().length == 0
                ? new RequestMethod[]{RequestMethod.GET, RequestMethod.HEAD, RequestMethod.POST,
                RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS,
                RequestMethod.TRACE}
                : Arrays.copyOf(mapping.method(), mapping.method().length);
    }

    private String join(String prefix, String path) {
        String joined = (prefix + "/" + path).replaceAll("/{2,}", "/");
        return joined.length() > 1 && joined.endsWith("/")
                ? joined.substring(0, joined.length() - 1)
                : joined;
    }
}
