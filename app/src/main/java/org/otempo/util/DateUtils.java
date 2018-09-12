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
package org.otempo.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Herramientas para trabajar con fechas
 */
public class DateUtils {

	/// Formato para los días de la semana que se visualizan encima de cada predicción
    public static final SimpleDateFormat weekDayFormat = new SimpleDateFormat("EEE d", Locale.getDefault());

    /**
     * Comprueba si una fecha dada se corresponde al día de hoy
     * @param cal La fecha a comprobar
     * @return True si es hoy, false en cualquier otro caso
     */
    public static boolean isToday(Calendar cal) {
        Calendar today  = Calendar.getInstance();
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
               && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Comprueba si una fecha es de hoy o un día posterior
     * @param cal La fecha a comprobar
     * @return True si es hoy a las 0:00 o más
     */
    public static boolean isFromToday(Calendar cal) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        return cal.after(yesterday);
    }

}
