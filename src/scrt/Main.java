/*******************************************************************************
 * Copyright (C) 2017-2018 C�sar Benito Lamata
 * 
 * This file is part of SCRT.
 * 
 * SCRT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SCRT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SCRT.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package scrt;

import scrt.ctc.Loader;
import scrt.gui.GUI;
import scrt.gui.editor.Editor;
import scrt.simulator.Simulator;

public class Main {
	public static void main(String[] args)
	{
		if(args.length == 0)
		{
			Simulator.init();
			Loader.load();
			scrt.regulation.Regulation.load();
			new GUI();
		}
		else new Editor();
	}
}
