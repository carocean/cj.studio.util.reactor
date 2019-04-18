package cj.studio.util.reactor;

 class DefaultPipeline implements IPipeline {
	LinkEntry head;
	String key;

	public DefaultPipeline(String key) {
		this.key = key;
	}

	@Override
	public String key() {
		return key;
	}

	@Override
	public void append(IValve valve) {
		if (head == null) {
			head = new LinkEntry(valve);
			return;
		}
		LinkEntry entry = getEndConstomerEntry();
		if (entry == null) {
			return;
		}
		LinkEntry lastEntry = entry.next;
		entry.next = new LinkEntry(valve);
		entry.next.next = lastEntry;
	}

	private LinkEntry getEndConstomerEntry() {
		if (head == null)
			return null;
		LinkEntry tmp = head;
		do {
			if (tmp.next == null)
				return tmp;
			tmp = tmp.next;
		} while (tmp != null);
		return null;
	}

	@Override
	public void remove(IValve valve) {
		if (head == null)
			return;
		LinkEntry tmp = head;
		do {
			if (valve.equals(tmp.next.entry)) {
				break;
			}
			tmp = tmp.next;
		} while (tmp.next != null);
		tmp.next = tmp.next.next;
	}

	@Override
	public void input(Event e) {
		if (head == null)
			return;
		nextFlow(e, null);
	}

	@Override
	public void nextFlow(Event e, IValve formthis) {
		if (head == null)
			return;
		if (formthis == null) {
			head.entry.flow(e, this);
			return;
		}
		LinkEntry linkEntry = lookforHead(formthis);
		if (linkEntry == null || linkEntry.next == null)
			return;
		linkEntry.next.entry.flow(e, this);
	}

	private LinkEntry lookforHead(IValve formthis) {
		if (head == null)
			return null;
		LinkEntry tmp = head;
		do {
			if (formthis.equals(tmp.entry)) {
				break;
			}
			tmp = tmp.next;
		} while (tmp.next != null);
		return tmp;
	}

	class LinkEntry {
		LinkEntry next;
		IValve entry;

		public LinkEntry(IValve entry) {
			this.entry = entry;
		}

	}
}
