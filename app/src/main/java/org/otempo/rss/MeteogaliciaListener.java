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
package org.otempo.rss;

import java.util.List;

import org.otempo.model.StationMediumTermPrediction;
import org.otempo.model.StationShortTermPrediction;

/**
 * Observador de carga de datos de meteogalicia. Sirve para hacer la carga en otro
 * thread diferente al de la UI.
 */
public interface MeteogaliciaListener {
    /**
     * Se llama aquí cuando se han cargado datos para la localización solicitada
     * Llamado desde un thread diferente al de la UI, CUIDADO!
     */
    void dataLoaded(List<StationShortTermPrediction> _shortPredictions,
                    List<StationMediumTermPrediction> _mediumPredictions);

    /**
     * Se llama aquí cuando falla la carga por error de internet
     * Llamado desde un thread diferente al de la UI, CUIDADO!
     */
    void internetError();

    /**
     * Se llama aquí cuando falla la carga por error del programador
     * Llamado desde un thread diferente al de la UI, CUIDADO!
     */
    void internalError();
}
