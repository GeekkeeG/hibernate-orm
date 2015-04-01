/*
 * This file is part of Hibernate Spatial, an extension to the
 *  hibernate ORM solution for spatial (geographic) data.
 *
 *  Copyright © 2007-2012 Geovise BVBA
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.hibernate.spatial.dialect.oracle;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;


import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.db.oracle.Decoders;
import org.geolatte.geom.codec.db.oracle.SDOGeometry;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.BasicExtractor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;


//TODO -- requires cleanup and must be made package local

/**
 * ValueExtractor for SDO_GEOMETRY
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/22/11
 */
public class SDOGeometryValueExtractor<X> extends BasicExtractor<X> {

	/**
	 * Creates instance
	 *
	 * @param javaDescriptor the {@code JavaTypeDescriptor} to use
	 * @param sqlTypeDescriptor the {@code SqlTypeDescriptor} to use
	 */
	public SDOGeometryValueExtractor(JavaTypeDescriptor<X> javaDescriptor, SqlTypeDescriptor sqlTypeDescriptor ) {
		super( javaDescriptor, sqlTypeDescriptor );
	}

	@Override
	protected X doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
		final Object geomObj = rs.getObject( name );
		return getJavaDescriptor().wrap( convert(geomObj), options );
	}

	@Override
	protected X doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
		final Object geomObj = statement.getObject( index );
		return getJavaDescriptor().wrap( convert(geomObj), options );
	}

	@Override
	protected X doExtract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
		final Object geomObj = statement.getObject( name );
		return getJavaDescriptor().wrap( convert(geomObj), options );
	}

	/**
	 * Converts an oracle to a JTS Geometry
	 *
	 * @param struct The Oracle STRUCT representation of an SDO_GEOMETRY
	 *
	 * @return The JTS Geometry value
	 */
	public Geometry convert(Object struct) {
		if ( struct == null ) {
			return null;
		}
		final SDOGeometry sdogeom = SDOGeometry.load( (Struct) struct );
		return toGeomerty(sdogeom);
	}

	private Geometry toGeomerty(SDOGeometry sdoGeom) {
        return Decoders.decode(sdoGeom);
    }

}