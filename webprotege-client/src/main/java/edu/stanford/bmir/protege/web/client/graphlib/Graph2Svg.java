package edu.stanford.bmir.protege.web.client.graphlib;

import com.google.gwt.core.client.GWT;
import edu.stanford.bmir.protege.web.client.viz.TextMeasurer;
import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.dom.Text;
import elemental.events.Event;
import elemental.svg.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 15 Oct 2018
 */
public class Graph2Svg {

    private static final String CLOSED_ARROW_HEAD_ID = "closedArrowHead";

    private static final String OPEN_ARROW_HEAD_ID = "openArrowHead";

    private static final String SVG_NS = "http://www.w3.org/2000/svg";

    @Nonnull
    private final TextMeasurer measurer;

    @Nonnull
    private final Graph graph;

    private BiConsumer<NodeDetails, Event> nodeClickHandler = (n, e) -> {};

    private BiConsumer<NodeDetails, Event> nodeDoubleClickHandler = (n, e) -> {};

    private BiConsumer<NodeDetails, Event> nodeContextMenuClickHandler = (n, e) -> {};

    private BiConsumer<NodeDetails, Event> nodeMouseOverHandler = (n, e) -> {};


    public Graph2Svg(@Nonnull TextMeasurer measurer, @Nonnull Graph graph) {
        this.measurer = checkNotNull(measurer);
        this.graph = checkNotNull(graph);
    }

    private static String inPixels(double i) {
        return Double.toString(i);
    }

    public Document getDocument() {
        return Browser.getDocument();
    }

    public void setNodeClickHandler(BiConsumer<NodeDetails, Event> nodeClickHandler) {
        this.nodeClickHandler = checkNotNull(nodeClickHandler);
    }

    public void setNodeDoubleClickHandler(BiConsumer<NodeDetails, Event> nodeDoubleClickHandler) {
        this.nodeDoubleClickHandler = checkNotNull(nodeDoubleClickHandler);
    }

    public void setNodeContextMenuClickHandler(BiConsumer<NodeDetails, Event> nodeContextMenuClickHandler) {
        this.nodeContextMenuClickHandler = checkNotNull(nodeContextMenuClickHandler);
    }

    public void setNodeMouseOverHandler(BiConsumer<NodeDetails, Event> nodeMouseOverHandler) {
        this.nodeMouseOverHandler = checkNotNull(nodeMouseOverHandler);
    }

    @Nonnull
    public Element createSvg() {
        Document document = getDocument();
        SVGElement svg = document.createSVGElement();
        // Arrow head defs
        SVGMarkerElement closedArrowHead = createArrowHeadMarker(document, CLOSED_ARROW_HEAD_ID, "wp-graph__edge__arrow-head wp-graph__edge__arrow-head--is-a", true);
        SVGMarkerElement openArrowHead = createArrowHeadMarker(document, OPEN_ARROW_HEAD_ID, "wp-graph__edge__arrow-head wp-graph__edge__arrow-head--rel", false);
        Element defsElement = document.createElementNS(SVG_NS, "defs");
        svg.appendChild(defsElement);
        defsElement.appendChild(openArrowHead);
        defsElement.appendChild(closedArrowHead);
        Element groupElement = document.createElementNS(SVG_NS, "g");
        svg.setAttribute("class", "wp-graph");
        svg.appendChild(groupElement);


        int w = graph.getWidth();
        int h = graph.getHeight();
        svg.setAttribute("viewbox", "0 0 " + w + " " + h);
        svg.setAttribute("preserveAspectRatio", "none");
        graph.getNodes()
                .map(this::toNodeSvgElement)
                .forEach(groupElement::appendChild);
        graph.getEdges()
                .flatMap(e -> toEdgeSvgElements(e).stream())
                .forEach(groupElement::appendChild);
        return svg;
    }

    private SVGMarkerElement createArrowHeadMarker(@Nonnull Document document,
                                                   @Nonnull String id,
                                                   @Nonnull String styleNames,
                                                   boolean closed) {
        SVGMarkerElement marker = getDocument().createSVGMarkerElement();
        marker.setId(id);
        marker.setAttribute("viewBox", "0 0 10 10");
        marker.setAttribute("markerWidth", "6");
        marker.setAttribute("markerHeight", "6");
        marker.setAttribute("refX", "9");
        marker.setAttribute("refY", "5");
        marker.setOrientToAuto();
        marker.setAttribute("class", styleNames);
        SVGPathElement markerPath = document.createSVGPathElement();
        SVGPathSegList markerSegments = markerPath.getPathSegList();
        markerSegments.appendItem(markerPath.createSVGPathSegMovetoAbs(1, 1));
        markerSegments.appendItem(markerPath.createSVGPathSegLinetoAbs(9, 5));
        markerSegments.appendItem(markerPath.createSVGPathSegLinetoAbs(1, 9));
        if (closed) {
            markerSegments.appendItem(markerPath.createSVGPathSegClosePath());
        }
        marker.appendChild(markerPath);
        return marker;
    }

    @Nonnull
    private Element toNodeSvgElement(@Nonnull NodeDetails nodeDetails) {
        Document document = getDocument();
        Element group = document.createElementNS(SVG_NS, "g");
        SVGRectElement rect = createRect(nodeDetails, document);
        SVGTextElement text = createText(nodeDetails);
        group.appendChild(text);
        group.appendChild(rect);
        return group;
    }

    private SVGRectElement createRect(@Nonnull NodeDetails nodeDetails, Document document) {
        SVGRectElement rectElement = document.createSVGRectElement();
        measurer.setStyleNames(nodeDetails.getStyleNames());
        double strokeWidth = measurer.getStrokeWidth();
        double halfStrokeWidth = strokeWidth / 2;
        rectElement.setAttribute("x", inPixels(nodeDetails.getTopLeftX() + halfStrokeWidth));
        rectElement.setAttribute("y", inPixels(nodeDetails.getTopLeftY() + halfStrokeWidth));
        rectElement.setAttribute("rx", "2");
        rectElement.setAttribute("ry", "2");
        if (nodeDetails.getWidth() > strokeWidth && nodeDetails.getHeight() > strokeWidth) {
            rectElement.setAttribute("width", inPixels(nodeDetails.getWidth() - strokeWidth));
            rectElement.setAttribute("height", inPixels(nodeDetails.getHeight() - strokeWidth));
        }
        rectElement.setAttribute("class", nodeDetails.getStyleNames());
        rectElement.setAttribute("pointer-events","visible");
        rectElement.addEventListener(Event.CLICK, evt -> nodeClickHandler.accept(nodeDetails, evt));
        rectElement.addEventListener(Event.DBLCLICK, evt -> nodeDoubleClickHandler.accept(nodeDetails, evt));
        rectElement.addEventListener(Event.CONTEXTMENU, evt -> nodeContextMenuClickHandler.accept(nodeDetails, evt));
        rectElement.addEventListener(Event.MOUSEOVER, evt -> nodeMouseOverHandler.accept(nodeDetails, evt));
        return rectElement;
    }

    @Nonnull
    private SVGTextElement createText(@Nonnull NodeDetails details) {
        return createTextElement(details.getLabel(), details.getX(), details.getY());
    }

    @Nonnull
    private SVGTextElement createText(@Nonnull EdgeDetails details) {
        return createTextElement(details.getLabel(), details.getX(), details.getY());
    }

    @Nonnull
    private SVGTextElement createTextElement(@Nonnull String text, int x, int y) {
        SVGTextElement textElement = getDocument().createSVGTextElement();
        Text textNode = getDocument().createTextNode(text);
        textElement.appendChild(textNode);
        textElement.setAttribute("text-anchor", "middle");
        textElement.setAttribute("alignment-baseline", "middle");
        textElement.setAttribute("fill", "var(--primary--color)");
        textElement.setAttribute("x", inPixels(x));
        textElement.setAttribute("y", inPixels(y));
        return textElement;
    }

    @Nonnull
    List<Element> toEdgeSvgElements(@Nonnull EdgeDetails edgeDetails) {
        GWT.log("[Graph2SVG] Converting Edge: " + edgeDetails.stringify());
        List<Point> points = edgeDetails.getPoints().collect(toList());
        SVGPathElement pathElement = getDocument().createSVGPathElement();
        pathElement.setAttribute("class", edgeDetails.getStyleNames());
        pathElement.setAttribute("fill", "none");
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if (i == 0) {
                SVGPathSegMovetoAbs move = pathElement.createSVGPathSegMovetoAbs(point.getX(), point.getY());
                pathElement.getPathSegList().appendItem(move);
            }
            else {
                SVGPathSegLinetoAbs lineTo = pathElement.createSVGPathSegLinetoAbs(point.getX(), point.getY());
                pathElement.getPathSegList().appendItem(lineTo);
            }
        }
        String arrowHeadId = "open".equalsIgnoreCase(edgeDetails.getArrowHeadStyle()) ? OPEN_ARROW_HEAD_ID : CLOSED_ARROW_HEAD_ID;
        pathElement.setAttribute("marker-end", "url(#" + arrowHeadId + ")");
        List<Element> edgeElements = new ArrayList<>();
        edgeElements.add(pathElement);

        // Edge label
        if (!edgeDetails.getLabel().isEmpty()) {
            int w = edgeDetails.getLabelWidth();
            int h = edgeDetails.getLabelHeight();
            SVGRectElement textRect = getDocument().createSVGRectElement();
            textRect.setAttribute("width", inPixels(w));
            textRect.setAttribute("height", inPixels(h));
            textRect.setAttribute("x", inPixels(edgeDetails.getX() - (w / 2.0)));
            textRect.setAttribute("y", inPixels(edgeDetails.getY() - (h / 2.0)));
            textRect.setAttribute("class", "wp-graph__edge__label");

            SVGTextElement text = createText(edgeDetails);
            Element labelGroup = getDocument().createElementNS(SVG_NS, "g");
            labelGroup.appendChild(textRect);
            labelGroup.appendChild(text);
            edgeElements.add(labelGroup);
        }
        return edgeElements;
    }

}
