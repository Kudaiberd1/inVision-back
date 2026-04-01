package com.u.invision.dto.response.dashboard;

public record ScoreOverviewResponse(
		int overall,
		double cvPoints,
		double essayPoints,
		double chatPoints,
		double cvLeadershipPoints,
		double cvProactivenessPoints,
		double cvEnergyPoints,
		double essayLeadershipPoints,
		double essayProactivenessPoints,
		double essayEnergyPoints,
		double chatLeadershipPoints,
		double chatProactivenessPoints,
		double chatEnergyPoints,
		Integer untScore,
		Double ieltsScore,
		Integer toeflScore,
		int untPoints,
		int ieltsPoints,
		int toeflPoints) {}

		