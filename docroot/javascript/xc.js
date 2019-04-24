//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 7245 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2011-11-09 10:12:24 -0500 (Wed, 09 Nov 2011) $
// $KeyWordsOff: $
//

/*
 * date:	2003-01-23
 * info:	http://inspire.server101.com/js/xc/
 */

var xcNode = [];

function xcSet(m, c) {
if (document.getElementById && document.createElement) {
	m = document.getElementById(m).getElementsByTagName('ul');
	var d, p, x, h, i, j;
	for (i = 0; i < m.length; i++) {
		if (d = m[i].getAttribute('id')) {
			xcCtrl(d, c, 'x', 'images/plus_blue.gif', '+', 'Show', m[i].getAttribute('title')+' (expand menu)');
			x = xcCtrl(d, c, 'c', 'images/minus_blue.gif', '-', 'Hide', m[i].getAttribute('title')+' (collapse menu)');

			p = m[i].parentNode;
			if (h = !p.className) {
				j = 2;
				while ((h = !(d == arguments[j])) && (j++ < arguments.length));
				if (h) {
					m[i].style.display = 'none';
					x = xcNode[d+'x'];
				}
			}

			p.className = c;
			p.insertBefore(x, p.firstChild);
		}
	}
}}


function xcShow(m) {
	xcXC(m, 'block', m+'c', m+'x');
}


function xcHide(m) {
	xcXC(m, 'none', m+'x', m+'c');
}


function xcXC(e, d, s, h) {
	e = document.getElementById(e);
	e.style.display = d;
	e.parentNode.replaceChild(xcNode[s], xcNode[h]);
	xcNode[s].firstChild.focus();
}

function xcCtrl(m, c, s, v, alt, f, t) {
	var a = document.createElement('a');
	a.setAttribute('href', 'javascript:xc'+f+'(\''+m+'\');');
	a.setAttribute('title', t);
	var i = document.createElement('img');
	i.setAttribute('src', v);
	i.setAttribute('alt', alt);
	i.setAttribute('border', '0');
	a.appendChild(i);

	var d = document.createElement('div');
	d.className = c+s;
	d.appendChild(a);

	return xcNode[m+s] = d;
}

/**********************************************
*
*    FUNCTIONS FOR MAKING THE LINKS WORK LIKE POST
*
**********************************************/

function doUnitPost(unitId) {
	window.document.nav_helper_form.unit_select.value=unitId;
	window.document.nav_helper_form.submit();
	return true;
}

function doSectionPost(sectionId) {
	window.document.nav_helper_form.section_select.value=sectionId;
	window.document.nav_helper_form.submit();
	return true;
}

function doSkillPost(skillId) {
	window.document.nav_helper_form.skill_select.value=skillId;
	window.document.nav_helper_form.submit();
	return true;
}

function doProblemPost(problemId) {
	window.document.nav_helper_form.problem_select.value=problemId;
	window.document.nav_helper_form.submit();
	return true;
}

function doCurriculumPost(currId) {
	window.document.nav_helper_form.curriculum_select.value=currId;
	window.document.nav_helper_form.submit();
	return true;
}
