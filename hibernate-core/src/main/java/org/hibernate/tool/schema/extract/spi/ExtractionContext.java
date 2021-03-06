/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2015, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.tool.schema.extract.spi;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.QualifiedSequenceName;
import org.hibernate.boot.model.relational.QualifiedTableName;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.service.ServiceRegistry;

/**
 * Defines a context for performing extraction including providing access to information about ongoing extraction as
 * well as to delegates needed in performing extraction.
 *
 * @author Steve Ebersole
 */
public interface ExtractionContext {
	ServiceRegistry getServiceRegistry();
	JdbcEnvironment getJdbcEnvironment();
	Connection getJdbcConnection();
	DatabaseMetaData getJdbcDatabaseMetaData();

	Identifier getDefaultCatalog();
	Identifier getDefaultSchema();

	public static interface DatabaseObjectAccess {
		public TableInformation locateTableInformation(QualifiedTableName tableName);
		public SequenceInformation locateSequenceInformation(QualifiedSequenceName sequenceName);
	}

	DatabaseObjectAccess getDatabaseObjectAccess();
}
