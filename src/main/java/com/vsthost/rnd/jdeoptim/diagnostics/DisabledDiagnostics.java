package com.vsthost.rnd.jdeoptim.diagnostics;

import com.vsthost.rnd.jdeoptim.evolution.Population;

public final class DisabledDiagnostics extends Diagnostics {
	public DisabledDiagnostics() {
		super(false, false);
	}

	@Override
	public void evolutionStarted() {
		// NOOP
	}

	@Override
	public void iterationStarted(final int iteration) {
		// NOOP
	}

	@Override
	public void iterationFinished(final int iteration, final Population population) {
		// NOOP
	}
}