/**
 * Copyright (c) 2012 by Thanos Kyritsis
 *
 * This file is part of eclass-javasearch.
 *
 * eclass-javasearch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * eclass-javasearch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with eclass-javasearch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 *
 */
package org.openeclass.lucene.demo;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class IndexCourses {

	private IndexCourses() {}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {

		String usage = "java org.openeclass.lucene.demo.IndexCourses"
				+ " [-index INDEX_PATH] [-update]\n\n"
				+ "This indexes the courses in Eclass DB, creating a Lucene index"
				+ "in INDEX_PATH that can be searched with SearchFiles";

		String indexPath = "data/eclass-index";
		boolean create = true;
		boolean help = false;

		for (int i = 0; i < args.length; i++) {
			if ("-index".equals(args[i])) {
				indexPath = args[i + 1];
				i++;
			} else if ("-update".equals(args[i])) {
				create = false;
			} else if ("-help".equals(args[i])) {
				help = true;
			}
		}

		if (help) {
			System.err.println("Usage: " + usage);
			System.exit(1);
		}

		Date start = new Date();
		try {
			
			System.out.println("Opening Database connection ...");
			
			Properties props = PropertyLoader.loadProperties("project-properties.xml");
            Connection con = DriverManager.getConnection(props.getProperty("jdbcurl"), props.getProperty("user"), props.getProperty("password"));
            con.setAutoCommit(false);
			
			System.out.println("Indexing to directory '" + indexPath + "'...");

			Directory dir = FSDirectory.open(new File(indexPath));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_23);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_23, analyzer);

			if (create) {
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}

			IndexWriter writer = new IndexWriter(dir, iwc);
			indexCourses(writer, con);

			writer.close();
			con.commit();
			con.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		} catch (SQLException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
			e.printStackTrace();
		}

	}

	private static void indexCourses(IndexWriter writer, Connection con) throws SQLException, IOException {
		
		PreparedStatement sql = con.prepareStatement("SELECT id, title, keywords, code, public_code, prof_names, created FROM course");
		ResultSet rs = sql.executeQuery();
		int c = 0;
		
		while (rs.next()) {
			
			Long id = rs.getLong(1);
			String title = rs.getString(2);
			String keys = rs.getString(3);
			String code = rs.getString(4);
			String publicCode = rs.getString(5);
			String profNames = rs.getString(6);
			//Timestamp created = rs.getTimestamp(7);
			
			Document doc = new Document();
			
			Field idField = new Field("course_id", id.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED);
			doc.add(idField);
			
			Field titleField = new Field("title", title, Field.Store.YES, Field.Index.ANALYZED);
			doc.add(titleField);
			
			Field keysField = new Field("keywords", keys, Field.Store.YES, Field.Index.ANALYZED);
			doc.add(keysField);
			
			Field codeField = new Field("code", code, Field.Store.YES, Field.Index.ANALYZED);
			doc.add(codeField);
			
			Field publicCodeField = new Field("public_code", publicCode, Field.Store.YES, Field.Index.ANALYZED);
			doc.add(publicCodeField);
			
			Field profsField = new Field("prof_names", profNames, Field.Store.YES, Field.Index.ANALYZED);
			doc.add(profsField);
			
			if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
				writer.addDocument(doc);
			} else {
				writer.updateDocument(new Term("course_id", id.toString()), doc);
			}
			
			c++;
		}
		
		System.out.println("total db rows: " + c);
		rs.close();
		sql.close();
	}

}
