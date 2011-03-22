/*
 * Copyright (C) 2010-2011 Ruben Lopez
 * 
 * This file is part of OTempo - Galician Weather
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package org.otempo.model;

/**
 * Patrón visitante para predicciones
 */
public interface StationPredictionVisitor {
    /// Procesa una predicción de corto plazo. Index es global, entre todas las predicciones de cualquier tipo (short+medium)
    public void apply(StationShortTermPrediction shortPred, int index);

    /// Procesa una predicción de largo plazo. Index es global, entre todas las predicciones de cualquier tipo (short+medium)
    public void apply(StationMediumTermPrediction medPred, int index);
}
