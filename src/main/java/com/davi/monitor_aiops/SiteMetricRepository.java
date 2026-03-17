package com.davi.monitor_aiops;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteMetricRepository extends JpaRepository<SiteMetric, Long> {
}