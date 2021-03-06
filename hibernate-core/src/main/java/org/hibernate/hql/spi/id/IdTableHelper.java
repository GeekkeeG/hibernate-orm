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
package org.hibernate.hql.spi.id;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.mapping.JoinedSubclass;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.Subclass;
import org.hibernate.mapping.UnionSubclass;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class IdTableHelper {
	private static final Logger log = Logger.getLogger( IdTableHelper.class );

	/**
	 * Singleton access
	 */
	public static final IdTableHelper INSTANCE = new IdTableHelper();

	private IdTableHelper() {
	}

	public boolean needsIdTable(PersistentClass entityBinding) {
		// need id table if the entity has secondary tables (joins)
		if ( entityBinding.getJoinClosureSpan() > 0 ) {
			return true;
		}

		// need an id table if the entity is part of either a JOINED or UNION inheritance
		// hierarchy.  We do not allow inheritance strategy mixing, so work on that assumption
		// here...
		final RootClass rootEntityBinding = entityBinding.getRootClass();
		final Iterator itr = rootEntityBinding.getSubclassIterator();
		if ( itr.hasNext() ) {
			final Subclass subclassEntityBinding = (Subclass) itr.next();
			if ( subclassEntityBinding instanceof JoinedSubclass || subclassEntityBinding instanceof UnionSubclass ) {
				return true;
			}
		}

		return false;
	}

	public void executeIdTableCreationStatements(
			List<String> creationStatements,
			JdbcServices jdbcServices,
			JdbcConnectionAccess connectionAccess) {
		try {
			Connection connection;
			try {
				connection = connectionAccess.obtainConnection();
			}
			catch (UnsupportedOperationException e) {
				// assume this comes from org.hibernate.engine.jdbc.connections.internal.UserSuppliedConnectionProviderImpl
				log.debug( "Unable to obtain JDBC connection; assuming ID tables already exist or wont be needed" );
				return;
			}

			try {
				// TODO: session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().createStatement();
				Statement statement = connection.createStatement();
				for ( String creationStatement : creationStatements ) {
					try {
						jdbcServices.getSqlStatementLogger().logStatement( creationStatement );
						// TODO: ResultSetExtractor
						statement.execute( creationStatement );
					}
					catch (SQLException e) {
						log.debugf( "Error attempting to export id-table [%s] : %s", creationStatement, e.getMessage() );
					}
				}

				// TODO
//				session.getTransactionCoordinator().getJdbcCoordinator().release( statement );
				statement.close();
			}
			catch (SQLException e) {
				log.error( "Unable to use JDBC Connection to create Statement", e );
			}
			finally {
				try {
					connectionAccess.releaseConnection( connection );
				}
				catch (SQLException ignore) {
				}
			}
		}
		catch (SQLException e) {
			log.error( "Unable obtain JDBC Connection", e );
		}
	}

	public void executeIdTableDropStatements(
			String[] dropStatements,
			JdbcServices jdbcServices,
			JdbcConnectionAccess connectionAccess) {
		if ( dropStatements == null ) {
			return;
		}

		try {
			Connection connection = connectionAccess.obtainConnection();

			try {
				// TODO: session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().createStatement();
				Statement statement = connection.createStatement();

				for ( String dropStatement : dropStatements ) {
					try {
						jdbcServices.getSqlStatementLogger().logStatement( dropStatement );
						statement.execute( dropStatement );
					}
					catch (SQLException e) {
						log.debugf( "Error attempting to cleanup id-table : [%s]", e.getMessage() );
					}
				}

				// TODO
//				session.getTransactionCoordinator().getJdbcCoordinator().release( statement );
				statement.close();
			}
			catch (SQLException e) {
				log.error( "Unable to use JDBC Connection to create Statement", e );
			}
			finally {
				try {
					connectionAccess.releaseConnection( connection );
				}
				catch (SQLException ignore) {
				}
			}
		}
		catch (SQLException e) {
			log.error( "Unable obtain JDBC Connection", e );
		}
	}

}
