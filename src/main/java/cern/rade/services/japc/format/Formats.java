package cern.rade.services.japc.format;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.web.socket.WebSocketHttpHeaders;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Formats {
    public static final List<Format> ALL = List.of(new JSONFormat(), new LabVIEWFormat());

    private Formats() {
    }

    public static Format preferred() {
        return ALL.get(0);
    }

    public static List<String> subprotocols() {
        return ALL.stream().map(Format::subprotocol).toList();
    }

    public static Format forSubprotocol(String subprotocol) {
        return ALL.stream().filter(format -> format.subprotocol().equals(subprotocol)).findFirst().orElse(null);
    }

    public static Format forHandshake(HttpHeaders headers) {
        List<String> offered = new WebSocketHttpHeaders(headers).getSecWebSocketProtocol();
        if (offered.isEmpty()) {
            return preferred();
        }
        return offered.stream().map(Formats::forSubprotocol).filter(format -> format != null).findFirst().orElse(null);
    }

    public static Format negotiate(HttpServletRequest request) {
        List<Format> candidates = new ArrayList<>();
        Format requestFormat = forMediaType(request.getContentType());
        if (requestFormat != null) {
            candidates.add(requestFormat);
        }
        candidates.addAll(ALL);
        for (MediaType accepted : accepted(request.getHeader(HttpHeaders.ACCEPT))) {
            for (Format format : candidates) {
                if (accepted.isCompatibleWith(format.mediaType())) {
                    return format;
                }
            }
        }
        return candidates.get(0);
    }

    private static Format forMediaType(String contentType) {
        if (contentType == null) {
            return null;
        }
        try {
            MediaType mediaType = MediaType.parseMediaType(contentType);
            return ALL.stream().filter(format -> format.mediaType().isCompatibleWith(mediaType)).findFirst().orElse(null);
        } catch (InvalidMediaTypeException e) {
            return null;
        }
    }

    private static List<MediaType> accepted(String accept) {
        List<MediaType> accepted;
        try {
            accepted = accept == null ? List.of(MediaType.ALL) : MediaType.parseMediaTypes(accept);
        } catch (InvalidMediaTypeException e) {
            accepted = List.of(MediaType.ALL);
        }
        return accepted.stream()
                .filter(mediaType -> mediaType.getQualityValue() > 0)
                .sorted(Comparator.comparingDouble(MediaType::getQualityValue).reversed()
                        .thenComparing(MediaType::isWildcardType)
                        .thenComparing(MediaType::isWildcardSubtype))
                .toList();
    }
}
