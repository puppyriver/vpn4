/**
 * Created by Ronnie on 2016/4/6.
 */

// _cacheJdbcTemplate
// _JdbcTemplate
// _pmDataParent
// _pmDataChilds
// _logger

var sql = "SELECT * FROM PM_DATA WHERE STATPOINTID = ? and TIMEPOINT > ? order by value desc";

var list = _cacheJdbcTemplate.queryForList(sql,_pmDataParent.statPointId,new java.util.Date(new Date().getTime() - 3600 * 24 * 1000));


_logger.info(" list size = "+list.size());

if (list.size() > 0) {
    var map = list.get(0);
    var pmData = new com.asb.pms.model.PM_DATA();
    pmData.setValue(map.VALUE);

    _pmDataChilds.add(pmData);

}