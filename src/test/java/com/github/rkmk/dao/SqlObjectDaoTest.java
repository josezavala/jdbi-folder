package com.github.rkmk.dao;

import com.github.rkmk.DaoTest;
import com.github.rkmk.mapper.BigDecimalMapperFactory;
import com.github.rkmk.mapper.CustomMapper;
import com.github.rkmk.mapper.CustomMapperFactory;
import com.github.rkmk.model.Movie;
import com.github.rkmk.model.Song;
import com.github.rkmk.model.Team;
import com.github.rkmk.model.primitiveBean;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Query;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;


public class SqlObjectDaoTest extends DaoTest {
    private SqlObjectDao dao;

    @Before
    public void setUp() {
        dao = handle.attach(SqlObjectDao.class);
    }

    @Test(expected = RuntimeException.class)
    public void shouldTakeRegisteredMapper() throws Exception {
        Movie jeans = Movie.builder().movieId(1).movieName("Jeans").build();
        insert(jeans);

        dao.getMovies();
    }

    @Test
    public void shouldUseOfCustomMappingFactory() throws Exception {
        Song song1 = Song.builder().songId(1).songName("Anbe Anbe").build();
        Song song2 = Song.builder().songId(2).songName("Columbus Columbus").build();
        insert(song1, song2);

        List<Song> songs = dao.getSongs();

        assertEquals(2, songs.size());
    }

    @Test
    public void shouldUseColumnNameWhileMapping() throws Exception {
        Team csk = Team.builder().teamId(1).teamName("CSK").build();
        Team rcb = Team.builder().teamId(2).teamName("RCB").build();
        Team mi  = Team.builder().teamId(3).teamName("MI") .build();
        insert(csk, rcb, mi);

        List<Team> teams = dao.getTeam();

        assertEquals(3, teams.size());
        assertEquals(new Integer(1), teams.get(0).getTeamId());
        assertEquals("CSK", teams.get(0).getTeamName());
        assertEquals(new Integer(2), teams.get(1).getTeamId());
        assertEquals("RCB", teams.get(1).getTeamName());
        assertEquals(new Integer(3), teams.get(2).getTeamId());
        assertEquals("MI", teams.get(2).getTeamName());
    }

    @Test
    public void shouldUseCustomMapperForFactoryForBigDecimal(){
        Team csk = new Team(1,"CSK",BigDecimal.ONE);
        insert(csk);

        DBI dbi = getDbi();

        CustomMapperFactory factory = new CustomMapperFactory();
        factory.register(new BigDecimalMapperFactory());
        dbi.registerMapper(factory);
        SqlObjectDao dao = dbi.open().attach(SqlObjectDao.class);

        List<Team> teams = dao.getTeam();

        assertEquals(1, teams.size());
        assertEquals(BigDecimal.TEN, teams.get(0).getAverage());
    }

    @Test
    public void shouldMapNullValueDefaultValueIfTypeIsPrimitive() throws Exception {
        handle.execute(" insert into primitive values(?,?,?,?,?,?,?,?,?,?)", null, null,null, null,null, null,null, null,null, null);
        Query<primitiveBean> map = handle.createQuery("select * from primitive")
                .map(new CustomMapper(primitiveBean.class));

        primitiveBean result = map.first();

        assertSame(0, result.getIntField());
        assertThat(result.getFloatField(), is(0.0f));
        assertThat(result.getDoubleField(), is(0.0d));
        assertThat(result.getLongField(), is(0l));
        assertFalse(result.isBooleanField());
        assertNull(result.getIntObjectField());
        assertNull(result.getFloatObjectField());
        assertNull(result.getDoubleObjectField());
        assertNull(result.getLongObjectField());
        assertNull(result.getBooleanObjectField());
    }
}
