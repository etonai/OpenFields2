/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;
import java.util.ArrayList;
import java.util.List;
import game.Unit;

/**
 * Manages unit selection and rectangle selection functionality.
 * Extracted from OpenFields2.java to improve code organization.
 */
public class SelectionManager {
    // Selection state
    private final List<Unit> selectedUnits = new ArrayList<>();
    private Unit selected = null; // Keep for backward compatibility
    private double selectionCenterX = 0;
    private double selectionCenterY = 0;
    
    // Rectangle selection state
    private boolean isSelecting = false;
    private double selectionStartX = 0;
    private double selectionStartY = 0;
    private double selectionEndX = 0; 
    private double selectionEndY = 0;
    
    /**
     * Calculates the center point of all selected units.
     * Updates selectionCenterX and selectionCenterY coordinates.
     */
    public void calculateSelectionCenter() {
        if (selectedUnits.isEmpty()) {
            selectionCenterX = 0;
            selectionCenterY = 0;
            return;
        }
        
        double sumX = 0, sumY = 0;
        for (Unit unit : selectedUnits) {
            sumX += unit.x;
            sumY += unit.y;
        }
        selectionCenterX = sumX / selectedUnits.size();
        selectionCenterY = sumY / selectedUnits.size();
    }
    
    /**
     * Finds all units within the current selection rectangle.
     * Clears existing selection and adds units found within the rectangle bounds.
     */
    public void findUnitsInRectangle(List<Unit> allUnits) {
        selectedUnits.clear();
        
        double minX = Math.min(selectionStartX, selectionEndX);
        double maxX = Math.max(selectionStartX, selectionEndX);
        double minY = Math.min(selectionStartY, selectionEndY);
        double maxY = Math.max(selectionStartY, selectionEndY);
        
        for (Unit unit : allUnits) {
            if (unit.x >= minX && unit.x <= maxX && unit.y >= minY && unit.y <= maxY) {
                selectedUnits.add(unit);
            }
        }
    }
    
    /**
     * Selects a single unit, clearing any existing selection.
     */
    public void selectUnit(Unit unit) {
        selectedUnits.clear();
        selectedUnits.add(unit);
        selected = unit; // Maintain backward compatibility
        calculateSelectionCenter();
    }
    
    /**
     * Clears all selected units.
     */
    public void clearSelection() {
        selectedUnits.clear();
        selected = null;
        selectionCenterX = 0;
        selectionCenterY = 0;
    }
    
    /**
     * Starts rectangle selection at the specified coordinates.
     */
    public void startRectangleSelection(double x, double y) {
        isSelecting = true;
        selectionStartX = x;
        selectionStartY = y;
        selectionEndX = x;
        selectionEndY = y;
    }
    
    /**
     * Updates the end point of the rectangle selection during drag.
     */
    public void updateRectangleSelection(double x, double y) {
        if (isSelecting) {
            selectionEndX = x;
            selectionEndY = y;
        }
    }
    
    /**
     * Completes the rectangle selection and finds units within the rectangle.
     */
    public void completeRectangleSelection(List<Unit> allUnits) {
        if (isSelecting) {
            findUnitsInRectangle(allUnits);
            calculateSelectionCenter();
            isSelecting = false;
            
            if (!selectedUnits.isEmpty()) {
                selected = selectedUnits.get(0); // Maintain backward compatibility
            }
        }
    }
    
    /**
     * Cancels the current rectangle selection without selecting units.
     */
    public void cancelRectangleSelection() {
        isSelecting = false;
    }
    
    /**
     * Renders selection-related graphics (rectangle selection and selection center marker).
     */
    public void render(GraphicsContext gc) {
        // Draw selection rectangle during drag
        if (isSelecting) {
            double minX = Math.min(selectionStartX, selectionEndX);
            double maxX = Math.max(selectionStartX, selectionEndX);
            double minY = Math.min(selectionStartY, selectionEndY);
            double maxY = Math.max(selectionStartY, selectionEndY);
            
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1);
            gc.strokeRect(minX, minY, maxX - minX, maxY - minY);
        }
        
        // Draw selection center marker (black filled circle) for multi-selection
        if (selectedUnits.size() > 1) {
            gc.setFill(Color.BLACK);
            gc.fillOval(selectionCenterX - 3, selectionCenterY - 3, 6, 6);
        }
    }
    
    /**
     * Resets all selection state (used for save/load operations).
     */
    public void reset() {
        selectedUnits.clear();
        selected = null;
        selectionCenterX = 0;
        selectionCenterY = 0;
        isSelecting = false;
    }
    
    // Getters
    public List<Unit> getSelectedUnits() {
        return selectedUnits;
    }
    
    public Unit getSelected() {
        return selected;
    }
    
    public double getSelectionCenterX() {
        return selectionCenterX;
    }
    
    public double getSelectionCenterY() {
        return selectionCenterY;
    }
    
    public boolean isSelecting() {
        return isSelecting;
    }
    
    public boolean hasSelection() {
        return !selectedUnits.isEmpty();
    }
    
    public int getSelectionCount() {
        return selectedUnits.size();
    }
    
    public boolean isUnitSelected(Unit unit) {
        return selectedUnits.contains(unit);
    }
    
    // Getters for rectangle selection coordinates
    public double getSelectionStartX() {
        return selectionStartX;
    }
    
    public double getSelectionStartY() {
        return selectionStartY;
    }
    
    public double getSelectionEndX() {
        return selectionEndX;
    }
    
    public double getSelectionEndY() {
        return selectionEndY;
    }
}